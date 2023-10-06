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
import io.nosqlbench.engine.core.lifecycle.scenario.context.ActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

/**
 * This is the core logic of every NB scenario.
 * <OL>
 * <LI>NBScenario creates a generic execution context.</LI>
 * <LI>This context is functionally applied to (executed by) a specific implementation.</LI>
 * <LI>Activities associated with the scenario are completed or errored.</LI>
 * <LI>A result is composited from the data in the component tree.</LI>
 * </OL>
 */
public abstract class NBScenario extends NBBaseComponent
    implements
    Function<NBSceneBuffer, ScenarioResult>,
    NBComponentErrorHandler {

    protected Logger logger = LogManager.getLogger("SCENARIO");
    private long startedAtMillis, endedAtMillis;

    private ScenarioMetadata scenarioMetadata;

    private ActivitiesController activitiesController;
    private Exception error;
    private String progressInterval = "console:10s";
    private ActivitiesProgressIndicator activitiesProgressIndicator;

    public NBScenario(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, NBLabels.forKV("scenario",scenarioName));
    }

    public String getScenarioName() {
        return getLabels().asMap().get("scenario");
    }

    public void forceStopScenario(int i, boolean b) {
        activitiesController.forceStopScenario(i,b);
    }

//    public Map<String, String> getParams() {
//        return this.params;
//    }

    public ActivitiesController getActivitiesController() {
        return this.activitiesController;
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
    public final ScenarioResult apply(NBSceneBuffer sctx) {
        this.activitiesController=sctx.controller();

        this.scenarioShutdownHook = new ScenarioShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(this.scenarioShutdownHook);

        this.state = NBScriptedScenario.State.Running;
        this.startedAtMillis = System.currentTimeMillis();
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .element(this)
                .now()
                .layer(Layer.Scenario)
                .build()
        );

        if (!"disabled".equals(progressInterval) && progressInterval!=null && !progressInterval.isEmpty())
            this.activitiesProgressIndicator = new ActivitiesProgressIndicator(activitiesController, this.progressInterval);

        ScenarioResult result = null;
        try {
            runScenario(sctx.asFixtures());
            final long awaitCompletionTime = 86400 * 365 * 1000L;
            this.logger.debug("Awaiting completion of scenario and activities for {} millis.", awaitCompletionTime);
            this.activitiesController.awaitCompletion(awaitCompletionTime);
        } catch (Exception e) {
            try {
                activitiesController.forceStopScenario(3000, false);
            } catch (final Exception eInner) {
                this.logger.debug("Found inner exception while forcing stop with rethrow=false: {}", eInner);
                throw new RuntimeException(e);
            }
            this.error = e;
        } finally {
            this.activitiesController.shutdown();
            this.endedAtMillis = System.currentTimeMillis();
            result = new ScenarioResult(
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

    protected abstract void runScenario(NBSceneFixtures sctx);

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
                activitiesController.getActivityExecutorMap().size()
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
}
