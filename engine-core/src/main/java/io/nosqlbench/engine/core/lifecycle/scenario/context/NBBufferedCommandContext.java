/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.scenario.context;

import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ContextShutdownHook;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Layer;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.NBBaseComponent;
import io.nosqlbench.nb.api.components.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NBBufferedCommandContext extends NBBaseComponent implements NBCommandContext {

    private final static Logger logger = LogManager.getLogger(NBBufferedCommandContext.class);
    private final ContextActivitiesController controller;
    private final ActivitiesProgressIndicator activitiesProgressIndicator;
    private ContextShutdownHook contextShutdownHook;
    private long startedAtMillis;
    private Exception error;
    private long endedAtMillis;
    private final Map<String, Object> vars = new LinkedHashMap<>();

    public enum IOType {
        connected,
        virtual,
        traced
    }

    private final IOType iotype;
    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    public NBBufferedCommandContext(NBComponent parent, String name, IOType ioTypes) {
        super(parent, NBLabels.forKV("context",name));
        this.iotype = ioTypes;
        this.controller = new ContextActivitiesController(this);

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

        this.contextShutdownHook = new ContextShutdownHook(this);

        Runtime.getRuntime().addShutdownHook(this.contextShutdownHook);

        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .element(this)
                .now()
                .layer(Layer.Scenario)
                .build()
        );

        this.activitiesProgressIndicator = new ActivitiesProgressIndicator(this.controller, "console:10s");
    }


    @Override
    public ContextActivitiesController controller() {
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

    public NBCommandContext asFixtures() {
        return (NBCommandContext) this;
    }


    public static ContextBuilderFacets.WantsName builder() {
        return new NBScenarioContextBuilder();
    }

    @Override
    public NBCommandResult apply(NBInvokableCommand nbCmd, NBCommandParams nbCmdParams) {
        NBCommandResult safeCmdResult = nbCmd.invokeSafe(this, nbCmdParams);
        error=safeCmdResult.getException();
        if (error!=null) {
            try {
                controller.forceStopActivities(3000);
            } catch (final Exception eInner) {
                logger.debug(() -> "Found inner exception while forcing stop activities with rethrow=false: " + eInner);
            }
//            throw new RuntimeException(safeCmdResult.getException());
        }

        Object object = safeCmdResult.getResultObject();
        String stepname = getLabels().valueOfOptional("step").orElse("unknownstep");
        if (object instanceof Map<?,?> map) {
            map.forEach((k,v) -> {
                logger.debug("setting command result for '" + stepname + "." + k + "' to '" + v.toString()+"'");
                getContainerVars().put(stepname+"."+k,v.toString());
            });
            getContainerVars().put(stepname+"._map",map);
        } else if (object instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                getContainerVars().put(stepname+"."+String.valueOf(i),list.get(i));
            }
            getContainerVars().put(stepname+"._list",list);
        } else if (object instanceof Set<?> set) {
            getContainerVars().put(stepname+"._set",set);
        } else if (object != null && object.getClass().isArray()) {
            getContainerVars().put(stepname + "._array", object);
        } else if (object !=null) {
            getContainerVars().put(stepname + "._object", object);

            // ignore
//        } else {
//
//            RuntimeException error = new RuntimeException("Unrecognizable type to set context vars with:" + object.getClass().getCanonicalName());
//            logger.error(error);
////            throw new RuntimeException("Unrecognizable type to set context vars with:" + object.getClass().getCanonicalName());
        } else {
            logger.debug("no object was provided to set the context result");
        }

        return safeCmdResult;
    }

    @Override
    public void doShutdown() {
        NBCommandContext.super.doShutdown();
    }

    @Override
    public Map<String, Object> getContainerVars() {
        return this.vars;
    }

    @Override
    public void beforeDetach() {
        // TODO, shutdown hooks need to be moved to context
        Runtime.getRuntime().removeShutdownHook(this.contextShutdownHook);
        final var retiringScenarioShutdownHook = this.contextShutdownHook;
        this.contextShutdownHook = null;
        retiringScenarioShutdownHook.run();
        this.logger.debug("removing context shutdown hook");
    }
}
