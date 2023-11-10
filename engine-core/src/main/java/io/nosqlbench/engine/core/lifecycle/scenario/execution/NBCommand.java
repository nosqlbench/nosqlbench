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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.metadata.ScenarioMetadata;
import io.nosqlbench.api.metadata.SystemId;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponentErrorHandler;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.BiFunction;

/**
 * This is a fragment of scenario logic, and can be applied to any scenario.
 * Each NBScenarioPhase comes fully parametrized. It is not meant to be invoked with different parameters.
 * Consider this single-shot for now.
 */
public abstract class NBCommand extends NBBaseComponent
    implements BiFunction<NBBufferedCommandContext, NBCommandParams, NBCommandResult>,
    NBComponentErrorHandler {

    private final String targetScenario;
    protected Logger logger = LogManager.getLogger("SCENARIO");
    private long startedAtMillis, endedAtMillis;

    private ScenarioMetadata scenarioMetadata;

    private ScenarioActivitiesController scenarioActivitiesController;
    private Exception error;
    private String progressInterval = "console:10s";
    private ActivitiesProgressIndicator activitiesProgressIndicator;

    public NBCommand(NBComponent parentComponent, String phaseName, String targetScenario) {
        super(parentComponent, NBLabels.forKV("command",phaseName));
        this.targetScenario = targetScenario;
    }

    public NBCommand(NBComponent parentComponent, String phaseName) {
        this(parentComponent, phaseName, "_testing_");
    }

    public String getScenarioName() {
        return getLabels().asMap().get("scenario");
    }

    public ScenarioActivitiesController getActivitiesController() {
        return this.scenarioActivitiesController;
    }

    public String getTargetScenario() {
        return this.targetScenario;
    }

    public enum State {
        Scheduled,
        Running,
        Errored,
        Interrupted,
        Finished
    }

    private ScenarioShutdownHook scenarioShutdownHook;
    private State state;

    /**
     * This should be the only way to get a ScenarioResult for a Scenario.
     * <p>
     * The lifecycle of a scenario includes the lifecycles of all of the following:
     * <OL>
     * <LI>The scenario control script, executing within a graaljs context.</LI>
     * <LI>The lifecycle of every activity which is started within the scenario.</LI>
     * </OL>
     * <p>
     * All of these run asynchronously within the scenario, however the same thread that calls
     * the scenario is the one which executes the control script. A scenario ends when all
     * of the following conditions are met:
     * <UL>
     * <LI>The scenario control script has run to completion, or experienced an exception.</LI>
     * <LI>Each activity has run to completion, experienced an exception, or all</LI>
     * </UL>
     *
     * @return
     */
    @Override
    public final NBCommandResult apply(NBBufferedCommandContext sctx, NBCommandParams params) {
        this.scenarioActivitiesController =sctx.controller();

        this.scenarioShutdownHook = new ScenarioShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(this.scenarioShutdownHook);

        this.state = NBScriptedCommand.State.Running;
        this.startedAtMillis = System.currentTimeMillis();
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .element(this)
                .now()
                .layer(Layer.Scenario)
                .build()
        );

        if (!"disabled".equals(progressInterval) && progressInterval!=null && !progressInterval.isEmpty())
            this.activitiesProgressIndicator = new ActivitiesProgressIndicator(scenarioActivitiesController, this.progressInterval);

        NBCommandResult result = null;
        try {
            invoke(params, sctx.out(),sctx.err(),sctx.in(),sctx.controller());
            final long awaitCompletionTime = 86400 * 365 * 1000L;
            this.logger.debug("Awaiting completion of scenario and activities for {} millis.", awaitCompletionTime);
            this.scenarioActivitiesController.awaitCompletion(awaitCompletionTime);
        } catch (Exception e) {
            try {
                scenarioActivitiesController.forceStopScenario(3000);
            } catch (final Exception eInner) {
                this.logger.debug("Found inner exception while forcing stop with rethrow=false: {}", eInner);
                throw new RuntimeException(e);
            }
            this.error = e;
        } finally {
            this.scenarioActivitiesController.shutdown();
            this.endedAtMillis = System.currentTimeMillis();
            result = new NBCommandResult(
                sctx,
                startedAtMillis,
                endedAtMillis,
                error
            );
        }


        Runtime.getRuntime().removeShutdownHook(this.scenarioShutdownHook);
        final var retiringScenarioShutdownHook = this.scenarioShutdownHook;
        this.scenarioShutdownHook = null;
        retiringScenarioShutdownHook.run();
        this.logger.debug("removing scenario shutdown hook");
        return result;
    }


    public void notifyException(final Thread t, final Throwable e) {
        error = new RuntimeException("in thread " + t.getName() + ", " + e, e);
    }

    public void finish() {
        this.logger.debug("finishing scenario");
        this.endedAtMillis = System.currentTimeMillis(); //TODO: Make only one endedAtMillis assignment
        if (State.Running == this.state) state = State.Finished;

        if (null != scenarioShutdownHook) {
            // If this method was called while the shutdown hook is defined, then it means
            // that the scenario was ended before the hook was uninstalled normally.
            state = State.Interrupted;
            this.logger.warn("Scenario was interrupted by process exit, shutting down");
        } else
            this.logger.info(
                "Scenario completed successfully, with {} logical activities.",
                scenarioActivitiesController.getActivityExecutorMap().size()
            );

        this.logger.info(() -> "scenario state: " + state);

        // We report the scenario state via annotation even for short runs
        final Annotation annotation = Annotation.newBuilder()
            .element(this)
            .interval(startedAtMillis, this.endedAtMillis)
            .layer(Layer.Scenario)
            .addDetail("event", "stop-scenario")
            .build();

        Annotators.recordAnnotation(annotation);

    }

    private synchronized ScenarioMetadata getScenarioMetadata() {
        if (null == this.scenarioMetadata) scenarioMetadata = new ScenarioMetadata(
            startedAtMillis,
            getScenarioName(),
            SystemId.getNodeId(),
            SystemId.getNodeFingerprint()
        );
        return this.scenarioMetadata;
    }

    @Override
    public String toString() {
        return "SCENARIO (" + this.getClass().getSuperclass().getSimpleName()+") { scenarioName: "+getScenarioName()+" }";
    }
    public abstract void invoke(
        NBCommandParams params,
        PrintWriter stdout,
        PrintWriter stderr,
        Reader stdin,
        ScenarioActivitiesController controller
    );


}
