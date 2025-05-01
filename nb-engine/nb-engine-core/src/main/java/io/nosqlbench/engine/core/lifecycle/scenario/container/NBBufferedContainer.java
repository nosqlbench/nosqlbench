/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.core.lifecycle.scenario.container;

import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ContextShutdownHook;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.advisor.NBAdvisorPoint;
import io.nosqlbench.nb.api.advisor.conditions.Conditions;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Layer;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBBufferedContainer extends NBBaseComponent implements NBContainer {

    private final static Logger logger = LogManager.getLogger(NBBufferedContainer.class);
    private final ContainerActivitiesController controller;
    private final ActivitiesProgressIndicator activitiesProgressIndicator;
    private ContextShutdownHook containerShutdownHook;
    private long startedAtMillis;
    private Exception error;
    private long endedAtMillis;
    private final Map<String, String> vars = new LinkedHashMap<>();
    private final List<NBAdvisorPoint<?>> advisors = new ArrayList<>();
    private NBAdvisorPoint<String> paramsAdvisor;

    public enum IOType {
        connected,
        virtual,
        traced
    }

    private final IOType iotype;
    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    public NBBufferedContainer(NBComponent parent, String name, IOType ioTypes) {
        super(parent, NBLabels.forKV("container", name));
        this.iotype = ioTypes;
        this.controller = new ContainerActivitiesController(this);

        switch (iotype) {
            case traced:
                stdoutBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stdout "), new PrintWriter(System.out));
                stderrBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stderr "), new PrintWriter(System.out));
                stdinBuffer = new DiagReader(new InputStreamReader(System.in), "  stdin ");
                break;
            case virtual:
                stdoutBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stdout "));
                stderrBuffer = new DiagWriter(new InterjectingCharArrayWriter(" stderr "));
                stdinBuffer = new DiagReader(new StringReader(""), "  stdin ");
                break;
            case connected:
                stdoutBuffer = new DiagWriter(new PrintWriter(System.out));
                stderrBuffer = new DiagWriter(new PrintWriter(System.out));
                stdinBuffer = new DiagReader(new InputStreamReader(System.in));
                break;
        }

        this.containerShutdownHook = new ContextShutdownHook(this);

        Runtime.getRuntime().addShutdownHook(this.containerShutdownHook);

        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .element(this)
                .now()
                .layer(Layer.Scenario)
                .build()
        );

        String progress = getComponentProp("progress").orElse("console:10s");
        this.activitiesProgressIndicator = new ActivitiesProgressIndicator(this.controller, progress);
    }


    @Override
    public ContainerActivitiesController controller() {
        return controller;
    }

    @Override
    public PrintWriter out() {
        return stdoutBuffer;
    }

    @Override
    public PrintWriter err() {
        return stderrBuffer;
    }

    @Override
    public Reader in() {
        return stdinBuffer;
    }

    public String getIOLog() {
        return this.stdoutBuffer.getTimedLog() + this.stderrBuffer.getTimedLog();
    }

    public static ContainerBuilderFacets.WantsName builder() {
        return new NBScenarioContainerBuilder();
    }

    @Override
    public NBCommandResult apply(NBInvokableCommand nbCmd, NBCommandParams nbCmdParams) {
        String stepname = nbCmd.getLabels().valueOfOptional("step").orElse("unknownstep");
        NBCommandParams interpolated = interpolate(nbCmdParams, this.getContainerVars(), stepname);
        NBCommandResult safeCmdResult = nbCmd.invokeSafe(this, interpolated);
        error = safeCmdResult.getException();
        if (error != null) {
            try {
                controller.forceStopActivities(3000);
            } catch (final Exception eInner) {
                logger.debug(() -> "Found inner exception while forcing stop activities with rethrow=false: " + eInner);
            }
//            throw new RuntimeException(safeCmdResult.getException());
        }

        Object object = safeCmdResult.getResultObject();
        applyResult(stepname, object);

        activitiesProgressIndicator.finish();
        return safeCmdResult;
    }

    private final static Pattern pattern = Pattern.compile("\\$\\{(?<name>[a-zA-Z_][a-zA-Z0-9_.]*)}");

    private NBCommandParams interpolate(NBCommandParams params, Map<String, String> vars, String stepname) {
        logger.debug("NBBufferedContainer.interpolate stepname=" + stepname);
        paramsAdvisor = create().advisor(b -> b.name("Check params"));
        paramsAdvisor.add(Conditions.ValidNameError);
        paramsAdvisor.validateAll(vars.keySet());
        Map<String, String> interpolated = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            Matcher varmatcher = pattern.matcher(v);
            StringBuilder sb = new StringBuilder();
            while (varmatcher.find()) {
                String varname = varmatcher.group("name");
                if (vars.containsKey(varname)) {
                    varmatcher.appendReplacement(sb, vars.get(varname));
                } else {
                    throw new RuntimeException("context var '" + varname + " was referenced in step '" + stepname + "', but it was not found in " + vars);
                }
            }
            varmatcher.appendTail(sb);
            interpolated.put(k, sb.toString());
        });
        paramsAdvisor.validateAll(interpolated.keySet());
        paramsAdvisor.setName("Container", "Check container and variable names")
            .logName().evaluate();
        return NBCommandParams.of(interpolated);
    }

    /**
     * Apply the result of a command execution to the container variables.
     *
     * @param stepname The step name of the command result.
     * @param object   The result object of the command execution.
     */
    private void applyResult(String stepname, Object object) {
        Map<String,String> results = new LinkedHashMap<>();
        if (object instanceof Record record) {
            HashSet<String> filtered = new HashSet<>(Set.of("hashCode","toString","getClass","notify","notifyAll","wait"));
            RecordComponent[] recordComponents = record.getClass().getRecordComponents();
            for (RecordComponent component : recordComponents) {
                try {
                    String name = component.getName();
                    Object value = component.getAccessor().invoke(record);
                    results.put(stepname+"."+name,value.toString());
                    filtered.add(name);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            List<Method> properties = Arrays.stream(record.getClass().getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> !filtered.contains(m.getName()))
                .toList();
            for (Method property : properties) {
                try {
                    Object value = property.invoke(record);
                    results.put(stepname+"."+property.getName(),value.toString());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (object instanceof InvokableResult ir) {
            ir.asResult().forEach((k, v) -> {
                logger.debug("setting InvokableResult command result for '" + stepname + "." + k + "' to '" + v + "'");
                results.put(stepname + "." + k, v);
            });
        } else if (object instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                logger.debug("setting command result for '" + stepname + "." + k + "' to '" + v.toString() + "'");
                results.put(stepname + "." + k, v.toString());
            });
        } else if (object instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                results.put(stepname + "." + i, list.get(i).toString());
            }
        } else if (object instanceof Set<?> set) {
            ArrayList<String> values = new ArrayList<>();
            for (Object o : set) {
                values.add(o.toString());
            }
            for (int i = 0; i < values.size(); i++) {
                results.put(stepname + "." + i, values.get(i));
            }
        } else if (object != null && object.getClass().isArray()) {
            Object[] ary = (Object[]) object;
            for (int i = 0; i < ary.length; i++) {
                results.put(stepname + "." + i, String.valueOf(ary[i]));
            }
        } else if (object != null) {
            results.put(stepname, object.toString());
        } else {
            logger.debug("no object was provided to set the container result");
        }
        logger.info("VARS:"+results);

        getContainerVars().putAll(results);
    }

    @Override
    public void doShutdown() {
        NBContainer.super.doShutdown();
    }

    @Override
    public Map<String, String> getContainerVars() {
        return this.vars;
    }

    @Override
    public void beforeDetach() {
        Runtime.getRuntime().removeShutdownHook(this.containerShutdownHook);
        final var retiringScenarioShutdownHook = this.containerShutdownHook;
        this.containerShutdownHook = null;
        retiringScenarioShutdownHook.run();
        logger.debug("removing container shutdown hook");
    }
}
