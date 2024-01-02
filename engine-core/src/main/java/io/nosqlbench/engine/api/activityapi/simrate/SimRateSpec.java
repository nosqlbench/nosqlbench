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

package io.nosqlbench.engine.api.activityapi.simrate;

import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.util.Unit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.temporal.ChronoUnit;

/**
 * <H2>Rate Limiter Specifications</H2>
 *
 * <P>A rate spec represent the configuration of a rate limiter. It is the event carrier
 * for applying changes to a rate limiter. For scripting purposes, rate limiters can be
 * controlled via assignment of a configuration string. A future version of the scripting
 * API will support direct method access. For now, the following semantics will apply
 * to the configuration value assigned to any of the rate parameters like cyclerate and striderate.
 * </P>
 *
 * <H2>Controlling Rate Limiters</H2>
 * <p>
 * Rate limiters specifiers can be easily constructed programmatically. However, in scripting,
 * these will often be controlled by assigning a configuration string.
 *
 * <P>
 * When a rate limiter is configured
 * using the configuration String, it can be in one of the following forms:
 *
 * <UL>
 * <LI>&lt;rate&gt;</LI>
 * <LI>&lt;rate&gt;,&lt;burst ratio&gt;</LI>
 * <LI>&lt;rate&gt;,&lt;burst ratio&gt;,&lt;verb&gt;</LI>
 * </UL>
 * <p>
 * Where:
 *
 * <EM>rate</EM> is the ops per second, expressed as any positive floating point value.
 * <EM>burst ratio</EM> is a floating point value greater than 1.0 which determines how much faster
 * the rate limiter may go to catch up to the overall.
 * <EM>verb</EM> is one of configure, start, or restart, as explained below.
 * <p>
 * For example:
 * <UL>
 * <LI>200 - allow up to 200 ops to start per second, with the default burst ratio of 1.1.
 * Start the rate limiter automatically.</LI>
 * <LI>3.6,2 - Allow up to 3.6 ops to start per second, but allow up to 7.2 ops per second
 * if needed to catch up. Start the rate limiter automatically </LI>
 * <LI>1000,1.05,restart - Allow up to 1000 ops per second on average, but allow 1050 ops per second
 * if the workload gets behind. If the rate limiter was already running, restart it, clearing any
 * previous backlog (wait time) and resource pools.</LI>
 * </UL>
 *
 * <H2>Rate Limiter Life Cycle</H2>
 *
 * <P>A rate limiter can be directly <EM>configured, started, restarted</EM>. Indirectly, rate
 * limiter construction and initialization occurs as needed.</p>
 *
 * <DL>
 *
 * <DT>(constructed)</DT>
 * <DD>Rate limiters are constructed and assigned automatically when they are first configured. A rate
 * limiter may not be explicitly constructed by the user.</DD>
 *
 * <DT>configure</DT>
 * <DD>When a rate limiter is configured, the rate spec is applied to it. If the rate limiters is
 * already running, then this new rate is applied to it. Applying the same rate parameters a
 * second time to a given rate limiter is a no-op.</DD>
 *
 * <DT>(initialization)</DT>
 * <DD>A rate limiter is initialized immediately before it is initially started. Rate limiters are also
 * re-initialized when they are restarted. When a rate limiter is initialized, the start time is set
 * to the current time, and the resource pools and accumulated wait time are zeroed. A rate limiter may
 * not be explicitly initialized by the user.
 * </DD>
 *
 * <DT>start</DT>
 * <DD>Starting a rate limiter activates the rate metering logic according to the current time. From
 * the time a rate limiter is started, any unused time will accumulate as wait time. A rate limiter is required
 * to be reset immediately before it is started for the first time.</DD>
 *
 * <DT>restart</DT>
 * <DD>Restarting a rate limiter is the same as starting it initially. The only difference is that
 * restarting forces a re-initialization as part of the configuration.</DD>
 * <p>
 * <HR/>
 * <P>ChronoUnit Examples</P>
 * <PRE>{@code
 * <p>
 * example ticks   op/s          seconds/op   ticks/op
 * -----------------------------------------------------------
 * 1       ns      50   ops/s    0.02, 1/20      20_000_000  (1s of ns / 50)
 * 2       ns       5   ops/s    0.2,  1/2      200_000_000  (1s of ns / 5)
 * 3       ns       0.5 ops/s    2.0   2/1    2_000_000_000  (1s of ns / 0.5)
 * 4       us       0.5 ops/s    2.0   2/1        2_000_000  (1s of us / 0.5)
 * 5       ms       0.5 ops/s    2.0   2/1            2_000  (1s of ms / 0.5)
 * }</PRE>
 *
 * <UL>
 * <LI>In examples 1 and 2, the ticks/op are comfortably within the 2147483648 (2^31)
 * range afforded by a 32-bit semaphore count.</LI>
 * <LI>Example 3 shows where the value
 * technically fits, but leaves no more than around 8% margin for the burst pool.
 * This is insufficient as burst pool should be allowed to be up to 100% the size
 * of the base rate pool.</LI>
 * <LI>This is remedied in examples 4 and 5 by adjusting the unit of allocation
 * to bigger chunks of time.</LI>
 * </UL>
 */
public class SimRateSpec {

    private final static Logger logger = LogManager.getLogger(SimRateSpec.class);
    public static final double DEFAULT_RATE_OPS_S = 1.0D;
    public static final double DEFAULT_BURST_RATIO = 1.1D;
    public static Verb DEFAULT_VERB = Verb.start;

    public ChronoUnit unit;

    /**
     * Target rate in Operations Per Second
     */
    public double opsPerSec = DEFAULT_RATE_OPS_S;
    public double burstRatio = DEFAULT_BURST_RATIO;
    public Verb verb = Verb.start;

    public double burstRatio() {
        return this.burstRatio;
    }

    public int ticksPerOp() {
        return switch (unit) {
            case NANOS -> (int) (1_000_000_000d / opsPerSec);
            case MICROS -> (int) (1_000_000d / opsPerSec);
            case MILLIS -> (int) (1_000d / opsPerSec);
            case SECONDS -> (int) (1d / opsPerSec);
            default -> throw new RuntimeException("invalid ChronoUnit for rate spec:" + unit);
        };
    }

    public int nanosToTicks(long newNanoTokens) {
//        if (newNanoTokens>Integer.MAX_VALUE) {
//            throw new RuntimeException("time base error with nanoseconds to ticks, value (" + newNanoTokens + ") is too large (>2^31!)");
//        }
        return switch (unit) {
            case NANOS -> (int) newNanoTokens;
            case MICROS -> (int) (newNanoTokens/1_000L);
            case MILLIS -> (int) (newNanoTokens/1_000_000L);
            case SECONDS -> (int) (newNanoTokens/1_000_000_000L);
            default -> throw new RuntimeException("invalid ChronoUnit for nanosToTicks:" + unit);
        };
    }

    public long ticksToNanos(int newTicks) {
        return switch (unit) {
            case NANOS -> newTicks;
            case MICROS -> newTicks*1_000L;
            case MILLIS -> newTicks*1_000_000L;
            case SECONDS -> newTicks*1_000_000_000L;
            default -> throw new RuntimeException("invalid ChronoUnit for ticksToNanos:" + unit);
        };
    }

    /**
     * Rate limiters can be put into motion in different modes to suit different scenarios. This is
     * mostly to support advanced scripting capability. When the verb is not specified in a {@link SimRateSpec},
     * then it is started immediately as a user would expect.
     */
    public enum Verb {
        /**
         * Specify that a rate limiter should only be configured without affecting its running state.
         * If the rate limiter is already running, then the configuration should take effect immediately.
         * A rate limiter will be created automatically if needed. Configurations that do not effectively
         * change the rate limiter are ignored. This does not automatically start the rate limiter. It
         * will need to be started explicitly before it is used.
         */
        configure,
        /**
         * The default behavior for a rate limiter. This means that the rate limiter should be started at the time
         * this rate spec is applied. The start time of a rate limiter is significant in that it determines
         * both the calculated average rate and the accumulated wait time from slow callers. In order
         * to start, a rate limiter will be configured automatically, if the provided rate spec would cause
         * a change to the configuration. If a rate limiter is started that is already running, an error should
         * be thrown. If it is desired to ignore this condition, the restart should be used instead.
         */
        start,
        /**
         * The restart action on a rate limiter causes it to be re-initialized as if it were just being
         * started for the first time. This causes any accumulated wait time or time resources to be zeroed.
         * This type of specifier can be useful, for example, when iterating on a workload with different
         * target rates, where each iteration is independent of the others. In order to restart, a rate
         * limiter will be configured if necessary.
         */
        restart,
        stop
    }

    public SimRateSpec(double opsPerSec, double burstRatio) {
        this(opsPerSec, burstRatio, DEFAULT_VERB);
    }

    public SimRateSpec(double opsPerSec, double burstRatio, Verb type) {
        apply(opsPerSec, burstRatio, verb);
    }

    private void apply(double opsPerSec, double burstRatio, Verb verb) {
        this.opsPerSec = opsPerSec;
        this.burstRatio = burstRatio;
        this.verb = verb;
        this.unit = chronoUnitFor(opsPerSec);

        // TODO: include burst into ticks calculation
    }

    private ChronoUnit chronoUnitFor(double opsPerSec) {
        if (opsPerSec > 1.0d) {
            return ChronoUnit.NANOS;
        }
        if (opsPerSec > 0.001d) {
            return ChronoUnit.MICROS;
        }
        if (opsPerSec > 0.000001d) {
            return ChronoUnit.MILLIS;
        }
        return ChronoUnit.SECONDS;
    }

    public SimRateSpec(ParameterMap.NamedParameter tuple) {
        this(tuple.value);
    }

    public SimRateSpec(String spec) {
        String[] specs = spec.split("[,:;]");
        Verb verb = Verb.start;
        double burstRatio = DEFAULT_BURST_RATIO;
        double opsPerSec;
        switch (specs.length) {
            case 3:
                verb = Verb.valueOf(specs[2].toLowerCase());
                logger.debug("selected rate limiter type: " + verb);
            case 2:
                burstRatio = Double.valueOf(specs[1]);
                if (burstRatio < 1.0) {
                    throw new RuntimeException("burst ratios less than 1.0 are invalid.");
                }
            case 1:
                opsPerSec = Unit.doubleCountFor(specs[0]).orElseThrow(() -> new RuntimeException("Unparsable:" + specs[0]));
                break;
            default:
                throw new RuntimeException("Rate specs must be either '<rate>' or '<rate>:<burstRatio>' as in 5000.0 or 5000.0:1.0");
        }
        apply(opsPerSec, burstRatio, verb);
    }

    public String toString() {
        double ratePortion = Math.abs(opsPerSec - ((long) opsPerSec));
        String ratefmt = (ratePortion > 0.001D) ? String.format("%,.3f", opsPerSec) : String.format("%,d", (long) opsPerSec);

        double br = burstRatio * opsPerSec;
        double burstPortion = Math.abs(br - ((long) br));
        String burstfmt = (burstPortion > 0.001D) ? String.format("%,.3f", br) : String.format("%,d", (long) br);

        return String.format("{ rate:'%s', burstRatio:'%.3f', SOPSS:'%s', BOPSS:'%s', verb:'%s' }", ratefmt, burstRatio, ratefmt, burstfmt, verb);
    }

    public double getRate() {
        return this.opsPerSec;
    }

    public double getBurstRatio() {
        return this.burstRatio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimRateSpec simRateSpec = (SimRateSpec) o;

        if (Double.compare(simRateSpec.opsPerSec, opsPerSec) != 0) return false;
        return Double.compare(simRateSpec.burstRatio, burstRatio) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(opsPerSec);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(burstRatio);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Verb getVerb() {
        return this.verb;
    }

    public boolean isAutoStart() {
        return this.verb == Verb.start || this.verb == Verb.restart;
    }

    public boolean isRestart() {
        return this.verb == Verb.restart;
    }


}
