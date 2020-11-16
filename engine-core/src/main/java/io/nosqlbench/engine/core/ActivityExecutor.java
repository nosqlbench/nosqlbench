/*
 *   Copyright 2015 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.nosqlbench.engine.core;

import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityimpl.input.ProgressCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>An ActivityExecutor is a named instance of an execution harness for a single activity instance.
 * It is responsible for managing threads and activity settings which may be changed while the activity is running.</p>
 *
 * <p>An ActivityExecutor may be represent an activity that is defined and active in the running
 * scenario, but which is inactive. This can occur when an activity is paused by controlling logic, or when the threads
 * are set to zero.</p>
 *
 * <p>
 * Invariants:
 * </p>
 * <ul>
 *     <li>Motors may not receive parameter updates before their owning activities are initialized.</li>
 * </ul>
 */

public class ActivityExecutor implements ActivityController, ParameterMap.Listener, ProgressCapable {

    private static final Logger logger = LoggerFactory.getLogger(ActivityExecutor.class);
    private static final Logger activitylogger = LoggerFactory.getLogger("ACTIVITY");

    private final List<Motor<?>> motors = new ArrayList<>();
    private final Activity activity;
    private final ActivityDef activityDef;
    private ExecutorService executorService;
    private RuntimeException stoppingException;

    private final static int waitTime = 10000;

//    private RunState intendedState = RunState.Uninitialized;

    public ActivityExecutor(Activity activity) {
        this.activity = activity;
        this.activityDef = activity.getActivityDef();
        executorService = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE,
            0L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new IndexedThreadFactory(activity.getAlias(), new ActivityExceptionHandler(this))
        );
        activity.getActivityDef().getParams().addListener(this);
        activity.setActivityController(this);
    }


    // TODO: Doc how uninitialized activities do not propagate parameter map changes and how
    // TODO: this is different from preventing modification to uninitialized activities

    /**
     * <p>True-up the number of motor instances known to the executor. Start all non-running motors.
     * The protocol between the motors and the executor should be safe as long as each state change is owned by either
     * the motor logic or the activity executor but not both, and strictly serialized as well. This is enforced by
     * forcing start(...) to be serialized as well as using CAS on the motor states.</p>
     * <p>The startActivity method may be called to true-up the number of active motors in an activity executor after
     * changes to threads.</p>
     */
    public synchronized void startActivity() {
        logger.info("starting activity " + activity.getAlias() + " for cycles " + activity.getCycleSummary());
        activitylogger.debug("START/before alias=(" + activity.getAlias() + ")");
        try {
            activity.setRunState(RunState.Starting);
            activity.initActivity();
            //activity.onActivityDefUpdate(activityDef);
        } catch (Exception e) {
            this.stoppingException = new RuntimeException("Error initializing activity '" + activity.getAlias() + "':\n" + e.getMessage(), e);
//            activitylogger.error("error initializing activity '" + activity.getAlias() + "': " + stoppingException);
            throw stoppingException;
        }
        adjustToActivityDef(activity.getActivityDef());
        activity.setRunState(RunState.Running);
        activitylogger.debug("START/after alias=(" + activity.getAlias() + ")");

    }

    /**
     * Simply stop the motors
     */
    public synchronized void stopActivity() {
        activitylogger.debug("STOP/before alias=(" + activity.getAlias() + ")");

        activity.setRunState(RunState.Stopping);
        logger.info("stopping activity in progress: " + this.getActivityDef().getAlias());
        motors.forEach(Motor::requestStop);
        motors.forEach(m -> awaitRequiredMotorState(m, 30000, 50, RunState.Stopped, RunState.Finished));
        activity.shutdownActivity();
        activity.closeAutoCloseables();
        activity.setRunState(RunState.Stopped);
        logger.info("stopped: " + this.getActivityDef().getAlias() + " with " + motors.size() + " slots");
        activitylogger.debug("STOP/after alias=(" + activity.getAlias() + ")");

    }

    public synchronized RuntimeException forceStopScenario(int initialMillisToWait) {
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
            logger.info("stopping activity forcibly " + activity.getAlias());
            List<Runnable> runnables = executorService.shutdownNow();
            long forcibleShutdownCompletedAt = System.currentTimeMillis();
            logger.debug("took " + (forcibleShutdownCompletedAt - gracefulWaitEndedAt) + " ms to shutdown forcibly");
            logger.debug(runnables.size() + " tasks never started.");
        }

        long activityShutdownStartedAt = System.currentTimeMillis();
        logger.debug("invoking activity-specific shutdown hooks");
        activity.shutdownActivity();
        activity.closeAutoCloseables();
        long activityShutdownEndedAt = System.currentTimeMillis();
        logger.debug("took " + (activityShutdownEndedAt - activityShutdownStartedAt) + " ms to shutdown activity threads");
        activitylogger.debug("FORCE STOP/after alias=(" + activity.getAlias() + ")");

        if (stoppingException != null) {
            activitylogger.debug("FORCE STOP/exception alias=(" + activity.getAlias() + ")");
        }
        return stoppingException;

    }

    /**
     * Shutdown the activity executor, with a grace period for the motor threads.
     *
     * @param initialMillisToWait milliseconds to wait after graceful shutdownActivity request, before forcing
     *                            everything to stop
     */
    public synchronized void forceStopScenarioAndThrow(int initialMillisToWait, boolean rethrow) {
        RuntimeException exception = forceStopScenario(initialMillisToWait);
        if (exception != null && rethrow) {
            throw exception;
        }
    }

    public boolean requestStopExecutor(int secondsToWait) {
        activitylogger.debug("REQUEST STOP/before alias=(" + activity.getAlias() + ")");


        logger.info("Stopping executor for " + activity.getAlias() + " when work completes.");

        executorService.shutdown();
        boolean wasStopped = false;
        try {
            logger.trace("awaiting termination with timeout of " + secondsToWait + " seconds");
            wasStopped = executorService.awaitTermination(secondsToWait, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.trace("interrupted while awaiting termination");
            wasStopped = false;
            logger.warn("while waiting termination of activity " + activity.getAlias() + ", " + ie.getMessage());
            activitylogger.debug("REQUEST STOP/exception alias=(" + activity.getAlias() + ") wasstopped=" + wasStopped);
        } finally {
            logger.trace("finally shutting down activity " + this.getActivity().getAlias());
            activity.shutdownActivity();
            logger.trace("closing auto-closeables");
            activity.closeAutoCloseables();
            activity.setRunState(RunState.Stopped);
        }
        if (stoppingException != null) {
            logger.trace("an exception caused the activity to stop:" + stoppingException.getMessage());
            throw stoppingException;
        }
        activitylogger.debug("REQUEST STOP/after alias=(" + activity.getAlias() + ") wasstopped=" + wasStopped);

        return wasStopped;
    }


    /**
     * Listens for changes to parameter maps, maps them to the activity instance, and notifies all eligible listeners of
     * changes.
     */
    @Override
    public synchronized void handleParameterMapUpdate(ParameterMap parameterMap) {

        if (activity instanceof ActivityDefObserver) {
            ((ActivityDefObserver) activity).onActivityDefUpdate(activityDef);
        }

        // An activity must be initialized before the motors and other components are
        // considered ready to handle parameter map changes. This is signaled in an activity
        // by the RunState.
        if (activity.getRunState() != RunState.Uninitialized) {
            if (activity.getRunState() == RunState.Running) {
                adjustToActivityDef(activity.getActivityDef());
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

    public boolean awaitCompletion(int waitTime) {
        return requestStopExecutor(waitTime);
    }

    public boolean awaitFinish(int timeout) {
        activitylogger.debug("AWAIT-FINISH/before alias=(" + activity.getAlias() + ")");

        boolean awaited = awaitAllRequiredMotorState(timeout, 50, RunState.Finished, RunState.Stopped);
        if (awaited) {
            awaited = awaitCompletion(timeout);
        }
        if (stoppingException != null) {
            activitylogger.debug("AWAIT-FINISH/exception alias=(" + activity.getAlias() + ")");
            throw stoppingException;
        }
        activitylogger.debug("AWAIT-FINISH/after alias=(" + activity.getAlias() + ")");
        return awaited;
    }

    public String toString() {
        return getClass().getSimpleName() + "~" + activityDef.getAlias();
    }

    private String getSlotStatus() {
        return motors.stream()
            .map(m -> m.getSlotStateTracker().getSlotState().getCode())
            .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Stop extra motors, start missing motors
     *
     * @param activityDef the activityDef for this activity instance
     */
    private synchronized void adjustToActivityDef(ActivityDef activityDef) {
        logger.trace(">-pre-adjust->" + getSlotStatus());

        // Stop and remove extra motor slots
        while (motors.size() > activityDef.getThreads()) {
            Motor motor = motors.get(motors.size() - 1);
            logger.trace("Stopping cycle motor thread:" + motor);
            motor.requestStop();
            motors.remove(motors.size() - 1);
        }

        // Create motor slots
        while (motors.size() < activityDef.getThreads()) {

            Motor motor = activity.getMotorDispenserDelegate().getMotor(activityDef, motors.size());
            logger.trace("Starting cycle motor thread:" + motor);
            motors.add(motor);
        }

        applyIntendedStateToDivergentMotors();
        awaitActivityAndMotorStateAlignment();

        logger.trace(">post-adjust->" + getSlotStatus());

    }

    private void applyIntendedStateToDivergentMotors() {
        RunState intended = activity.getRunState();
        logger.trace("ADJUSTING to INTENDED " + intended);
        switch (intended) {
            case Uninitialized:
                break;
            case Running:
            case Starting:
                motors.stream()
                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Running)
                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Finished)
                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Starting)
                    .forEach(m -> {
                        m.getSlotStateTracker().enterState(RunState.Starting);
                        executorService.execute(m);
                    });
                break;
            case Stopped:
                motors.stream()
                    .filter(m -> m.getSlotStateTracker().getSlotState() != RunState.Stopped)
                    .forEach(Motor::requestStop);
                break;
            case Finished:
            case Stopping:
                throw new RuntimeException("Invalid requested state in activity executor:" + activity.getRunState());

            default:
                throw new RuntimeException("Unmatched run state:" + activity.getRunState());
        }
    }

    private void awaitActivityAndMotorStateAlignment() {

        switch (activity.getRunState()) {
            case Starting:
            case Running:
                motors.forEach(m -> awaitRequiredMotorState(m, waitTime, 50, RunState.Running, RunState.Finished));
                break;
            case Stopped:
                motors.forEach(m -> awaitRequiredMotorState(m, waitTime, 50, RunState.Stopped, RunState.Finished));
                break;
            case Uninitialized:
                break;
            case Finished:
                motors.forEach(m -> awaitRequiredMotorState(m, waitTime, 50, RunState.Finished));
                break;
            case Stopping:
                throw new RuntimeException("Invalid requested state in activity executor:" + activity.getRunState());
            default:
                throw new RuntimeException("Unmatched run state:" + activity.getRunState());
        }
        logger.debug("activity and threads are aligned to state " + activity.getRunState() + " for " + this.getActivity().getAlias());

    }

    /**
     * Await a thread (aka motor/slot) entering a specific SlotState
     *
     * @param m                motor instance
     * @param waitTime         milliseconds to wait, total
     * @param pollTime         polling interval between state checks
     * @param desiredRunStates any desired SlotState
     * @return true, if the desired SlotState was detected
     */
    private boolean awaitMotorState(Motor m, int waitTime, int pollTime, RunState... desiredRunStates) {
        Set<RunState> desiredStates = new HashSet<>(Arrays.asList(desiredRunStates));

        long startedAt = System.currentTimeMillis();
        while (System.currentTimeMillis() < (startedAt + waitTime)) {
            Map<RunState, Integer> actualStates = new HashMap<>();
            for (RunState state : desiredRunStates) {
                actualStates.compute(state, (k, v) -> (v == null ? 0 : v) + 1);
            }
            for (RunState desiredRunState : desiredRunStates) {
                actualStates.remove(desiredRunState);
            }
            logger.trace("state of remaining slots:" + actualStates.toString());
            if (actualStates.size() == 0) {
                return true;
            } else {
                System.out.println("motor states:" + actualStates.toString());
                try {
                    Thread.sleep(pollTime);
                } catch (InterruptedException ignored) {
                }
            }
        }
        logger.trace(activityDef.getAlias() + "/Motor[" + m.getSlotId() + "] is now in state " + m.getSlotStateTracker().getSlotState());
        return false;
    }


    private boolean awaitAllRequiredMotorState(int waitTime, int pollTime, RunState... awaitingState) {
        long startedAt = System.currentTimeMillis();
        boolean awaited = false;
        while (!awaited && (System.currentTimeMillis() < (startedAt + waitTime))) {
            awaited = true;
            for (Motor motor : motors) {
                awaited = awaitMotorState(motor, waitTime, pollTime, awaitingState);
                if (!awaited) {
                    logger.trace("failed awaiting motor " + motor.getSlotId() + " for state in " +
                        Arrays.asList(awaitingState));
                    break;
                }
            }
        }
        return awaited;
    }


    private boolean awaitAnyRequiredMotorState(int waitTime, int pollTime, RunState... awaitingState) {
        long startedAt = System.currentTimeMillis();
        while (System.currentTimeMillis() < (startedAt + waitTime)) {
            for (Motor motor : motors) {
                for (RunState state : awaitingState) {
                    if (motor.getSlotStateTracker().getSlotState() == state) {
                        logger.trace("at least one 'any' of " + activityDef.getAlias() + "/Motor[" + motor.getSlotId() + "] is now in state " + motor.getSlotStateTracker().getSlotState());
                        return true;
                    }
                }
            }
            try {
                Thread.sleep(pollTime);
            } catch (InterruptedException ignored) {
            }
        }
        logger.trace("none of " + activityDef.getAlias() + "/Motor [" + motors.size() + "] is in states in " + Arrays.asList(awaitingState));
        return false;
    }


    /**
     * Await a required thread (aka motor/slot) entering a specific SlotState
     *
     * @param m             motor instance
     * @param waitTime      milliseconds to wait, total
     * @param pollTime      polling interval between state checks
     * @param awaitingState desired SlotState
     * @throws RuntimeException if the waitTime is used up and the desired state is not reached
     */
    private void awaitRequiredMotorState(Motor m, int waitTime, int pollTime, RunState... awaitingState) {
        RunState startingState = m.getSlotStateTracker().getSlotState();
        boolean awaitedRequiredState = awaitMotorState(m, waitTime, pollTime, awaitingState);
        if (!awaitedRequiredState) {
            String error = "Unable to await " + activityDef.getAlias() +
                "/Motor[" + m.getSlotId() + "]: from state " + startingState + " to " + m.getSlotStateTracker().getSlotState()
                + " after waiting for " + waitTime + "ms";
            RuntimeException e = new RuntimeException(error);
            logger.error(error);
            throw e;
        }
        logger.trace("motor " + m + " entered awaited state: " + Arrays.asList(awaitingState));
    }

    private synchronized void requestStopMotors() {
        logger.info("stopping activity " + activity);
        activity.setRunState(RunState.Stopped);
        motors.forEach(Motor::requestStop);
    }


    public boolean isRunning() {
        return motors.stream().anyMatch(m -> m.getSlotStateTracker().getSlotState() == RunState.Running);
    }

    public Activity getActivity() {
        return activity;
    }

    public synchronized void notifyException(Thread t, Throwable e) {
        //logger.error("Uncaught exception in activity thread forwarded to activity executor:", e);
        this.stoppingException = new RuntimeException("Error in activity thread " + t.getName(), e);
        forceStopScenario(10000);
    }

    @Override
    public synchronized void stopActivityWithReasonAsync(String reason) {
        logger.info("Stopping activity " + this.activityDef.getAlias() + ": " + reason);
        this.stoppingException = new RuntimeException("Stopping activity " + this.activityDef.getAlias() + ": " + reason);
        logger.error("stopping with reason: " + stoppingException);
        requestStopMotors();
    }

    @Override
    public synchronized void stopActivityWithErrorAsync(Throwable throwable) {
        if (stoppingException == null) {
            this.stoppingException = new RuntimeException(throwable);
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
    public ProgressMeter getProgressMeter() {
        return this.activity.getProgressMeter();
    }


}
