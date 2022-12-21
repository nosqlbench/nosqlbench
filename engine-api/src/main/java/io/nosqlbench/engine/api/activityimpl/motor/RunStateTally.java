/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl.motor;

import io.nosqlbench.engine.api.activityapi.core.RunState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * <H2>Synopsis</H2>
 * <P>Event-oriented tally of the runtime states of all
 * motor threads. This is meant to be used as the definitive
 * aggregate scorecard for an activity's thread states.</P>
 * <HR></HR>
 * <H2>Purpose</H2>
 * <P>This limits inter-thread signaling requirements by constraining
 * the cases for which blockers are notified. This is because the
 * meaningful scenarios for which a blocker would want to be notified
 * do not include changes of positive values to other positive values.
 * This allows an explicit optimization around scenarios for which a
 * state count increments to 1 or decrements to 0, as in "some" or "none".
 * This is an effective optimization for scenarios with many active
 * threads.
 * </P>
 * <HR></HR>
 * <H2>Calling Semantics</H2>
 * <P>Callers of the await functions will block for the required condition or,
 * if specified, the timeout to occur without the condition.
 * These callers are unblocked atomically, after any state add, state remove,
 * or state change events are fully completed.</P>
 * <P>{@link RunStateImage} is returned from blocking methods so that
 * callers can know consistently what the current run states were at
 * the time their condition was met or timed out. Any callers of such
 * methods <EM>must</EM> check for whether the condition was successfully
 * met via {@link RunStateImage#isTimeout()}</P>
 * <HR></HR>
 * <H2>Invariants</H2>
 * <UL>
 *     <LI>Under consistent usage patterns, all counts should be zero or positive at all times.</LI>
 *     <LI>The current count for each state should be equal to the visible {@link RunState} on each
 *     motor thread, as all transitions are made through the motor threads directly.</LI>
 * </UL>
 */
public class RunStateTally {
    private final static Logger logger = LogManager.getLogger("TALLY");

    /**
     * If no timeout is given for any of the await methods, then the default is to wait for
     * approximately many eons. Some tests run until told to stop.
     */
    public final long DEFAULT_TIMEOUT_MS=Long.MAX_VALUE;

    private final int[] counts = new int[RunState.values().length];

    /**
     * @return the current count for the specified state
     * @param runState The {@link RunState} to count
     *
     */
    public synchronized int tallyFor(RunState runState) {
        return counts[runState.ordinal()];
    }

    /**
     * Signal that a motor thread has left one {@link RunState} and entered another,
     * atomically. After both counts are updated, if any RunState counts changed to or from zero,
     * then all potential observers are notified to re-evaluate their await conditions.
     * @param from the prior RunState
     * @param to the next RunState
     */
    public synchronized void change(RunState from, RunState to) {
        counts[from.ordinal()]--;
        counts[to.ordinal()]++;
        logger.trace(() -> this +" -"+from+ String.format(":%04d",counts[from.ordinal()])+ ", +"+to+ String.format(":%04d",counts[to.ordinal()]));

        if (counts[from.ordinal()]==0 || counts[to.ordinal()]==1) {
            logger.debug(() -> "NOTIFYing on edge "+
                "from " + from + String.format(":%04d",counts[from.ordinal()]) +
                " to " + to + String.format(":%04d",counts[to.ordinal()]));
            notifyAll();
        }
    }

    /**
     * Add a previously untracked motor thread to state tracking with the provided {@link RunState}.
     * If the given state's count becomes non-zero, then all potential observers are notified to re-evaluate
     * their await conditions.
     * @param state The initial tracking state for the related motor thread
     */
    public synchronized void add(RunState state) {
        counts[state.ordinal()]++;
        logger.trace(() -> this +" +"+state+ String.format(":%04d",counts[state.ordinal()]));
        if (counts[state.ordinal()]==1) {
            logger.debug(() -> "NOTIFYing on ++-SOME edge for " + state + String.format(":%04d",counts[state.ordinal()]));
            notifyAll();
        }
    }

    /**
     * Remove a previously tracked motor thread from state tracking with the provided {@link RunState}.
     * If the given state's count becomes zero, then all potential observers are notified to re-evaluate
     * their await conditions.
     * @param state The final tracking state for the related motor thread
     */
    public synchronized void remove(RunState state) {
        counts[state.ordinal()]--;
        logger.trace(() -> this +" -"+state+ String.format(":%04d",counts[state.ordinal()]));
        if (counts[state.ordinal()]==0) {
            logger.debug(() -> "NOTIFYing on 00-NONE edge for " + state + String.format(":%04d",counts[state.ordinal()]));
            notifyAll();
        }
    }

    /**
     * Await until all states but the provided {@link RunState}s have zero counts.
     * This condition matches if the provided states have zero or positive counts <EM>if and only if</EM>
     * all other states have zero counts. Thus, if there are no positive values at all, this condition will
     * still match.
     * @param runStates The states which <EM>may</EM> have zero counts and still match the condition
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitNoneOther(RunState... runStates) {
        return this.awaitNoneOther(DEFAULT_TIMEOUT_MS, runStates);
    }
    /**
     * Exactly like {@link #awaitNoneOther(long, RunState...)}, except that it allows for a timeout,
     * after which the method will unblock and signal an error if the await condition is still false.
     * @param runStates RunStates which are the only valid states before unblocking
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitNoneOther(long timeoutMillis, RunState... runStates) {
        logger.debug(() -> "☐ Awaiting only " + Arrays.toString(runStates) + " for " + timeoutMillis+"ms");
        long timeoutAt = timeoutAt(timeoutMillis);

        int sum=0;
        for (RunState runState: RunState.values()) {
            sum+=counts[runState.ordinal()];
        }
        for (RunState runState : runStates) {
            sum-=counts[runState.ordinal()];
        }
        while (sum>0 && System.currentTimeMillis()<timeoutAt) {
            try {
                wait(timeoutAt-System.currentTimeMillis());
            } catch (InterruptedException e) {
            }

            sum=0;
            for (RunState runState: RunState.values()) {
                sum+=counts[runState.ordinal()];
            }
            for (RunState runState : runStates) {
                sum-=counts[runState.ordinal()];
            }
        }

        boolean timedout = (sum!=0);
        logger.debug(() -> (timedout ? "✘ TIMED-OUT awaiting only " : "☑ Awaited only " ) + toString(runStates));
        return new RunStateImage(this.counts,timedout);
    }

    private long timeoutAt(long timeoutMillis) {
        long delayTill= System.currentTimeMillis() + timeoutMillis;
        return (delayTill>0) ? delayTill : Long.MAX_VALUE;
    }
    /**
     * Await until there are zero counts for all of the specified {@link RunState}s.
     * @param runStates all RunStates which must be zeroed before unblocking
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitNoneOf(RunState... runStates) {
        return this.awaitNoneOf(DEFAULT_TIMEOUT_MS, runStates);
    }
    /**
     * Exactly like {@link #awaitNoneOf(RunState...)}, except that it allows for a timeout, after which the
     * method will unblock and signal a timeout if the await condition is still not met.
     * @param runStates all RunStates which must be zeroed before unblocking
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitNoneOf(long timeoutMillis, RunState... runStates) {
        logger.debug(() -> "☐ Awaiting none of " + Arrays.toString(runStates)+ " for " + timeoutMillis+"ms");
        long timeoutAt = timeoutAt(timeoutMillis);

        int sum=0;
        for (RunState runState : runStates) {
            sum+=counts[runState.ordinal()];
        }
        while (sum>0 && System.currentTimeMillis()<timeoutAt) {
            try {
                wait(timeoutAt-System.currentTimeMillis());
            } catch (InterruptedException e) {
            }
            sum=0;
            for (RunState runState : runStates) {
                sum+=counts[runState.ordinal()];
            }
        }
        boolean timedout=sum==0;
        logger.debug(() -> (timedout ? "✘ TIMED-OUT awaiting none of " : "☑ Awaited none of " ) + toString(runStates));
        return new RunStateImage(this.counts,timedout);
    }

    /**
     * Await until at least one of the provided runStates has a positive value.
     * @param runStates RunStates any of which allow unblocking
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitAny(RunState... runStates) {
        return this.awaitAny(DEFAULT_TIMEOUT_MS,runStates);
    }

    /**
     * Exactly like {@link #awaitAny(RunState...)}, except that it allows for a timeout, after which
     * the method will unblock and signal a timeout if the await condition is still not met.
     * @param runStates RunStates any of which allow unblocking
     * @param timeoutMillis Milliseconds to wait for any of the runstates before giving up
     * @return A {@link RunStateImage}, indicating success or failure, and the view of states at the time of evaluation
     */
    public synchronized RunStateImage awaitAny(long timeoutMillis, RunState... runStates) {
        logger.debug(() -> "☐ Awaiting any " + Arrays.toString(runStates) + " for " + timeoutMillis + "ms");
        long timeoutAt = timeoutAt(timeoutMillis);

        while (System.currentTimeMillis()<timeoutAt) {
            for (RunState runState : runStates) {
                if (counts[runState.ordinal()]>0) {
                    logger.debug("☑ Awaited any " + toString(runStates));
                    return new RunStateImage(this.counts,false);
                }
            }
            try {
                wait(timeoutAt-System.currentTimeMillis());
            } catch (InterruptedException e) {
            }
        }
        logger.debug(() -> "✘ TIMED-OUT awaiting any of " + toString(runStates));
        return new RunStateImage(this.counts,true);
    }

    public String toString(RunState... runStates) {
        StringBuilder sb = new StringBuilder();
        for (RunState runState : runStates) {
            sb.append(runState.getCode()).append("(").append(counts[runState.ordinal()]).append(") ");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
    public String toString() {
        return toString(RunState.values());
    }
}
