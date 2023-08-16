/*
 * Copyright (c) 2022-2023 nosqlbench
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
package io.nosqlbench.engine.core.lifecycle.activity;

import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityimpl.MotorState;
import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateImage;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.IndexedThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>An ActivityExecutor is an execution harness for a single activity instance.
 * It is responsible for managing threads and activity settings which may be changed while the activity is running.</p>
 *
 * <p>In order to allow for dynamic thread management, which is not easily supported as an explicit feature
 * of most executor services, threads are started as long-running processes and managed via state signaling.
 * The {@link RunState} enum, {@link MotorState} type, and {@link RunStateTally}
 * state tracking class are used together to represent valid states and transitions, contain and transition state atomically,
 * and provide blocking conditions for observers, respectively.</p>
 *
 * <P>Some basic rules and invariants must be observed for consistent concurrent behavior.
 * Any state changes for a Motor must be made through {@link Motor#getState()}.
 * This allows the state tracking to work consistently for all observers.</p>
 */

public class ActivityExecutor implements ActivityController, ParameterMap.Listener, ProgressCapable, Callable<ExecutionResult> {

    // TODO Encapsulate valid state transitions to be only modifiable within the appropriate type view.

    private static final Logger logger = LogManager.getLogger(ActivityExecutor.class);
    private static final Logger activitylogger = LogManager.getLogger("ACTIVITY");

    private final List<Motor<?>> motors = new ArrayList<>();
    private final Activity activity;
    private final ActivityDef activityDef;
    private final RunStateTally tally;
    private ExecutorService executorService;
    private Exception exception;
    private String sessionId = "";
    private long startedAt = 0L;
    private long stoppedAt = 0L;

    public ActivityExecutor(Activity activity, String sessionId) {
        this.activity = activity;
        this.activityDef = activity.getActivityDef();
        activity.getActivityDef().getParams().addListener(this);
        activity.setActivityController(this);
        this.sessionId = sessionId;
        this.tally = activity.getRunStateTally();
    }

    // TODO: Doc how uninitialized activities do not propagate parameter map changes and how
    // TODO: this is different from preventing modification to uninitialized activities

    // TODO: Determine whether this should really be synchronized

    /**
     * Simply stop the motors
     */
     public void stopActivity() {
        logger.info(() -> "stopping activity in progress: " + this.getActivityDef().getAlias());

        activity.setRunState(RunState.Stopping);
        motors.forEach(Motor::requestStop);
        tally.awaitNoneOther(RunState.Stopped, RunState.Finished, RunState.Errored);

        shutdownExecutorService(Integer.MAX_VALUE);
        tally.awaitNoneOther(RunState.Stopped, RunState.Finished, RunState.Errored);
        activity.setRunState(RunState.Stopped);

        logger.info(() -> "stopped: " + this.getActivityDef().getAlias() + " with " + motors.size() + " slots");

        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(sessionId)
            .interval(this.startedAt, this.stoppedAt)
            .layer(Layer.Activity)
            .label("alias", getActivityDef().getAlias())
            .label("driver", getActivityDef().getActivityType())
            .label("workload", getActivityDef().getParams().getOptionalString("workload").orElse("none"))
            .detail("params", getActivityDef().toString())
            .build()
        );
    }

    /**
     * Force stop the motors without trying to wait for the activity to reach stopped/finished state
     */
    public void forceStopActivity() {
        logger.info(() -> "force stopping activity in progress: " + this.getActivityDef().getAlias());

        activity.setRunState(RunState.Stopping);
        motors.forEach(Motor::requestStop);

        shutdownExecutorService(Integer.MAX_VALUE);
        tally.awaitNoneOther(RunState.Stopped, RunState.Finished);
        activity.setRunState(RunState.Stopped);

        logger.info(() -> "stopped: " + this.getActivityDef().getAlias() + " with " + motors.size() + " slots");

        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(sessionId)
            .interval(this.startedAt, this.stoppedAt)
            .layer(Layer.Activity)
            .label("alias", getActivityDef().getAlias())
            .label("driver", getActivityDef().getActivityType())
            .label("workload", getActivityDef().getParams().getOptionalString("workload").orElse("none"))
            .detail("params", getActivityDef().toString())
            .build()
        );
    }

    public Exception forceStopActivity(int initialMillisToWait) {

        activitylogger.debug("FORCE STOP/before alias=(" + activity.getAlias() + ")");
        activity.setRunState(RunState.Stopped);

        executorService.shutdown();
        requestStopMotors();

        int divisor = 100;
        int polltime = initialMillisToWait / divisor;
        long gracefulWaitStartedAt = System.currentTimeMillis();
        long waitUntil = initialMillisToWait + gracefulWaitStartedAt;
        long time = gracefulWaitStartedAt;
        while (time < waitUntil && !executorService.isTerminated()) {
            try {
                Thread.sleep(polltime);
                time = System.currentTimeMillis();
            } catch (InterruptedException ignored) {
            }
        }
        long gracefulWaitEndedAt = System.currentTimeMillis();
        logger.debug("took " + (gracefulWaitEndedAt - gracefulWaitStartedAt) + " ms to shutdown gracefully");

        if (!executorService.isTerminated()) {
            logger.info(() -> "stopping activity forcibly " + activity.getAlias());
            List<Runnable> runnables = executorService.shutdownNow();
            long forcibleShutdownCompletedAt = System.currentTimeMillis();
            logger.debug(() -> "took " + (forcibleShutdownCompletedAt - gracefulWaitEndedAt) + " ms to shutdown forcibly");
            logger.debug(() -> runnables.size() + " tasks never started.");
        }

        long activityShutdownStartedAt = System.currentTimeMillis();
        logger.debug("invoking activity-specific shutdown hooks");
        activity.shutdownActivity();
        activity.closeAutoCloseables();
        long activityShutdownEndedAt = System.currentTimeMillis();
        logger.debug("took " + (activityShutdownEndedAt - activityShutdownStartedAt) + " ms to shutdown activity threads");
        activitylogger.debug("FORCE STOP/after alias=(" + activity.getAlias() + ")");

        if (exception != null) {
            activitylogger.debug("FORCE STOP/exception alias=(" + activity.getAlias() + ")");
        }
        return exception;

    }

    /**
     * Shutdown the activity executor, with a grace period for the motor threads.
     *
     * @param initialMillisToWait
     *     milliseconds to wait after graceful shutdownActivity request, before forcing
     *     everything to stop
     */
    public synchronized void forceStopScenarioAndThrow(int initialMillisToWait, boolean rethrow) {
        Exception exception = forceStopActivity(initialMillisToWait);
        if (exception != null && rethrow) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Listens for changes to parameter maps, maps them to the activity instance, and notifies all eligible listeners of
     * changes.
     */
    @Override
    public void handleParameterMapUpdate(ParameterMap parameterMap) {

        activity.onActivityDefUpdate(activityDef);

        // An activity must be initialized before the motors and other components are
        // considered ready to handle parameter map changes. This is signaled in an activity
        // by the RunState.
        if (activity.getRunState() != RunState.Uninitialized) {
            if (activity.getRunState() == RunState.Running) {
                adjustMotorCountToThreadParam(activity.getActivityDef());
            }
            motors.stream()
                .filter(m -> (m instanceof ActivityDefObserver))
//                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Uninitialized)
//                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Starting)
                .forEach(m -> ((ActivityDefObserver) m).onActivityDefUpdate(activityDef));
        }
    }

    public ActivityDef getActivityDef() {
        return activityDef;
    }

    public String toString() {
        return getClass().getSimpleName() + "~" + activityDef.getAlias();
    }

    private String getSlotStatus() {
        return motors.stream()
            .map(m -> m.getState().get().getCode())
            .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Stop extra motors, start missing motors
     *
     * @param activityDef
     *     the activityDef for this activity instance
     */
    private void adjustMotorCountToThreadParam(ActivityDef activityDef) {
        logger.trace(() -> ">-pre-adjust->" + getSlotStatus());

        reduceActiveMotorCountDownToThreadParam(activityDef);
        increaseActiveMotorCountUpToThreadParam(activityDef);
        alignMotorStateToIntendedActivityState();
        awaitAlignmentOfMotorStateToActivityState();

        logger.trace(() -> ">post-adjust->" + getSlotStatus());

    }

    private void increaseActiveMotorCountUpToThreadParam(ActivityDef activityDef) {
        // Create motor slots
        while (motors.size() < activityDef.getThreads()) {

            Motor motor = activity.getMotorDispenserDelegate().getMotor(activityDef, motors.size());
            logger.trace(() -> "Starting cycle motor thread:" + motor);
            motors.add(motor);
        }
    }

    private void reduceActiveMotorCountDownToThreadParam(ActivityDef activityDef) {
        // Stop and remove extra motor slots
        while (motors.size() > activityDef.getThreads()) {
            Motor motor = motors.get(motors.size() - 1);
            logger.trace(() -> "Stopping cycle motor thread:" + motor);
            motor.requestStop();
            motor.removeState();

            /**
             * NOTE: this leaves trailing, longer-running threads which might hold the executor open
             * to potentially be cleaned up by {@link ExecutorService#shutdown()} or
             * {@link ExecutorService#shutdownNow()}. At this point, the motor thread has
             * been instructed to shutdown, and it is effectively thread-non-grata to the activity.
             */
            motors.remove(motors.size() - 1);
        }
    }

    private synchronized void alignMotorStateToIntendedActivityState() {
        RunState intended = activity.getRunState();
        logger.trace(() -> "ADJUSTING to INTENDED " + intended);
        switch (intended) {
            case Uninitialized:
                break;
            case Running:
            case Starting:
                motors.stream()
                    .filter(m -> m.getState().get() != RunState.Running)
                    .filter(m -> m.getState().get() != RunState.Finished)
                    .filter(m -> m.getState().get() != RunState.Starting)
                    .forEach(m -> {
                        executorService.execute(m);
                    });
                break;
            case Stopped:
                motors.stream()
                    .filter(m -> m.getState().get() != RunState.Stopped)
                    .forEach(Motor::requestStop);
                break;
            case Finished:
            case Stopping:
            case Errored:
                break;
//                throw new RuntimeException("Invalid requested state in activity executor:" + activity.getRunState());

            default:
                throw new RuntimeException("Unmatched run state:" + activity.getRunState());
        }
    }

    private void awaitAlignmentOfMotorStateToActivityState() {

        logger.debug(() -> "awaiting state alignment from " + activity.getRunState());
        RunStateImage states = null;
        switch (activity.getRunState()) {
            case Starting:
            case Running:
                states = tally.awaitNoneOther(RunState.Running, RunState.Finished);
                break;
            case Errored:
            case Stopping:
            case Stopped:
                states = tally.awaitNoneOther(RunState.Stopped, RunState.Finished, RunState.Errored);
                break;
            case Uninitialized:
                break;
            case Finished:
                states = tally.awaitNoneOther(RunState.Finished);
                break;
            default:
                throw new RuntimeException("Unmatched run state:" + activity.getRunState());
        }
        RunState previousState = activity.getRunState();
        activity.setRunState(states.getMaxState());
        logger.debug("activity and threads are aligned to state " + previousState + " for " + this.getActivity().getAlias() + ", and advanced to " + activity.getRunState());
    }


    private void requestStopMotors() {
        logger.info(() -> "stopping activity " + activity);
        activity.setRunState(RunState.Stopping);
        motors.forEach(Motor::requestStop);
    }


    public boolean isRunning() {
        return motors.stream().anyMatch(m -> m.getState().get() == RunState.Running);
    }

    public Activity getActivity() {
        return activity;
    }

    public synchronized void notifyException(Thread t, Throwable e) {
        logger.debug(() -> "Uncaught exception in activity thread forwarded to activity executor: " + e.getMessage());
        this.exception = new RuntimeException("Error in activity thread " + t.getName(), e);
        this.requestStopMotors();
    }

    @Override
    public synchronized void stopActivityWithReasonAsync(String reason) {
        logger.info(() -> "Stopping activity " + this.activityDef.getAlias() + ": " + reason);
        this.exception = new RuntimeException("Stopping activity " + this.activityDef.getAlias() + ": " + reason);
        logger.error("stopping with reason: " + exception);
        requestStopMotors();
    }

    @Override
    public synchronized void stopActivityWithErrorAsync(Throwable throwable) {
        if (exception == null) {
            this.exception = new RuntimeException(throwable);
            logger.error("stopping on error: " + throwable.toString(), throwable);
        } else {
            if (activityDef.getParams().getOptionalBoolean("fullerrors").orElse(false)) {
                logger.error("additional error: " + throwable.toString(), throwable);
            } else {
                logger.warn("summarized error (fullerrors=false): " + throwable.toString());
            }
        }
        requestStopMotors();
    }

    @Override
    public ProgressMeterDisplay getProgressMeter() {
        return this.activity.getProgressMeter();
    }


    @Override
    public ExecutionResult call() throws Exception {

        try {
            // instantiate and configure fixtures that need to be present
            // before threads start running such as metrics instruments
            activity.initActivity();
            startMotorExecutorService();
            startRunningActivityThreads();
            awaitMotorsAtLeastRunning();
            logger.debug("STARTED " + activityDef.getAlias());
            awaitActivityCompletion();
        } catch (Exception e) {
            this.exception = e;
        } finally {
            activity.shutdownActivity();
            activity.closeAutoCloseables();
        }
        ExecutionResult result = new ExecutionResult(startedAt, stoppedAt, "", exception);
        return result;
    }

    /**
     * This waits for at least one motor to be in running, finished or stopped state.
     * A motor with enough cycles to read will go into a running state. A motor which has
     * a short read immediately after being started will go into a finished state. A motor
     * which has been stopped for some reason, like an error or a stop command will go into
     * stopped state. All of these states are sufficient to signal that successful startup
     * has been completed at least.
     */
    private void awaitMotorsAtLeastRunning() {
        RunStateImage states = tally.awaitAny(RunState.Running, RunState.Stopped, RunState.Finished, RunState.Errored);
        RunState maxState = states.getMaxState();
        if (maxState == RunState.Errored) {
            activity.setRunState(maxState);
            throw new RuntimeException("Error in activity");
        }
    }

    public synchronized void startActivity() {
        RunStateImage startable = tally.awaitNoneOther(1000L, RunState.Uninitialized, RunState.Stopped);
        if (startable.isTimeout()) {
            throw new RuntimeException("Unable to start activity '" + getActivity().getAlias() + "' which is in state " + startable);
        }
        startMotorExecutorService();
        startRunningActivityThreads();
        awaitMotorsAtLeastRunning();
    }

    private boolean shutdownExecutorService(int secondsToWait) {

        activitylogger.debug(() -> "Shutting down motor executor for (" + activity.getAlias() + ")");

        boolean wasStopped = false;
        try {
            executorService.shutdown();
            logger.trace(() -> "awaiting termination with timeout of " + secondsToWait + " seconds");
            wasStopped = executorService.awaitTermination(secondsToWait, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.trace("interrupted while awaiting termination");
            logger.warn("while waiting termination of shutdown " + activity.getAlias() + ", " + ie.getMessage());
            activitylogger.debug("REQUEST STOP/exception alias=(" + activity.getAlias() + ") wasstopped=" + wasStopped);
        } catch (RuntimeException e) {
            logger.trace("Received exception while awaiting termination: " + e.getMessage());
            wasStopped = true;
            exception = e;
        } finally {
            logger.trace(() -> "finally shutting down activity " + this.getActivity().getAlias());
            this.stoppedAt = System.currentTimeMillis();
            activity.setRunState(RunState.Stopped);
        }

        if (exception != null) {
            logger.trace(() -> "an exception caused the activity to stop:" + exception.getMessage());
            logger.warn("Setting ERROR on motor executor for activity '" + activity.getAlias() + "': " + exception.getMessage());
            throw new RuntimeException(exception);
        }

        activitylogger.debug("motor executor for " + activity.getAlias() + ") wasstopped=" + wasStopped);

        return wasStopped;
    }

    private void awaitActivityCompletion() {
        RunStateImage state = tally.awaitNoneOther(RunState.Stopped, RunState.Finished, RunState.Errored);
        RunState maxState = state.getMaxState();
        activity.setRunState(maxState);
        if (maxState == RunState.Errored) {
            throw new RuntimeException("Error while waiting for activity completion:" + this.exception);
        }
    }

    private void startMotorExecutorService() {
        this.executorService = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE,
            0L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new IndexedThreadFactory(activity.getAlias(), new ActivityExceptionHandler(this))
        );
    }


    /**
     * <p>True-up the number of motor instances known to the executor. Start all non-running motors.
     * The protocol between the motors and the executor should be safe as long as each state change is owned by either
     * the motor logic or the activity executor but not both, and strictly serialized as well. This is enforced by
     * forcing start(...) to be serialized as well as using CAS on the motor states.</p>
     * <p>The startActivity method may be called to true-up the number of active motors in an activity executor after
     * changes to threads.</p>
     */
    private void startRunningActivityThreads() {

        logger.info(() -> "starting activity " + activity.getAlias() + " for cycles " + activity.getCycleSummary());
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(sessionId)
            .now()
            .layer(Layer.Activity)
            .label("alias", getActivityDef().getAlias())
            .label("driver", getActivityDef().getActivityType())
            .label("workload", getActivityDef().getParams().getOptionalString("workload").orElse("none"))
            .detail("params", getActivityDef().toString())
            .build()
        );

        activitylogger.debug("START/before alias=(" + activity.getAlias() + ")");

        try {
            activity.setRunState(RunState.Starting);
            this.startedAt = System.currentTimeMillis();
            activity.onActivityDefUpdate(activityDef);
        } catch (Exception e) {
            this.exception = new RuntimeException("Error initializing activity '" + activity.getAlias() + "':\n" + e.getMessage(), e);
            activitylogger.error(() -> "error initializing activity '" + activity.getAlias() + "': " + exception);
            throw new RuntimeException(exception);
        }
        adjustMotorCountToThreadParam(activity.getActivityDef());
        tally.awaitAny(RunState.Running, RunState.Finished, RunState.Stopped);
        activity.setRunState(RunState.Running);
        activitylogger.debug("START/after alias=(" + activity.getAlias() + ")");
    }


}
