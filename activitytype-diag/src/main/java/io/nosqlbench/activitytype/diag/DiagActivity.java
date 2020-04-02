/*
 *   Copyright 2016 jshook
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
package io.nosqlbench.activitytype.diag;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiters;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateSpec;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.virtdata.core.bindings.VirtData;

import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

public class DiagActivity extends SimpleActivity implements Activity, ActivityDefObserver {

    public Counter pendingOpsCounter;
    protected Histogram delayHistogram;
    private RateLimiter diagRateLimiter;
    private boolean async = false;
    private long maxAsync;

    private LongToIntFunction resultFunc = new ResultFunc_Modulo128();
    private LongUnaryOperator delayFunc = new DelayFunc_NoDelay();
    private SequenceBlocker sequenceBlocker;


    public DiagActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        delayHistogram = ActivityMetrics.histogram(activityDef, "diagdelay");
        Integer initdelay = activityDef.getParams().getOptionalInteger("initdelay").orElse(0);
        try {
            Thread.sleep(initdelay);
        } catch (InterruptedException ignored) {
        }
        //onActivityDefUpdate(activityDef);
        if (isAsync()) {
            pendingOpsCounter = ActivityMetrics.counter(this.activityDef, "pending_ops");
        }
    }

    public RateLimiter getDiagRateLimiter() {
        return diagRateLimiter;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        activityDef.getParams()
                .getOptionalString("diagrate")
                .map(RateSpec::new)
                .ifPresent(spec -> diagRateLimiter = RateLimiters.createOrUpdate(getActivityDef(), "diag", diagRateLimiter, spec));


        // Allow for a programmable delay function, in nanoseconds

        this.delayFunc =
                activityDef.getParams().getOptionalString("delayfunc")
                        .map(m -> VirtData.getFunction(m, LongUnaryOperator.class))
                        .orElse(null);

        if (delayFunc==null) {
            delayFunc=new DelayFunc_NoDelay();
        }

        // Allow for a programmable result function, but support the previous shortcuts

        activityDef.getParams().assertOnlyOneOf("resultfunc", "resultmodulo", "staticvalue");


        activityDef.getParams().getOptionalString("resultfunc")
                .map(m -> VirtData.getFunction(m, LongToIntFunction.class))
                .ifPresent(f -> this.resultFunc = f);

        activityDef.getParams().getOptionalString("resultmodulo")
                .map(Long::valueOf)
                .map(ResultFunc_ResultModulo::new)
                .ifPresent(f -> this.resultFunc=f);

        activityDef.getParams().getOptionalString("staticvalue")
                .map(Long::valueOf)
                .map(ResultFunc_StaticValue::new)
                .ifPresent(f -> this.resultFunc=f);

        if (this.resultFunc==null) {
            this.resultFunc=new ResultFunc_Modulo128();
        }
    }

    public LongToIntFunction getResultFunc() {
        return resultFunc;
    }

    public LongUnaryOperator getDelayFunc() {
        return delayFunc;
    }


    public boolean isAsync() {
        return activityDef.getParams().getOptionalInteger("async").isPresent();
    }

    public long getMaxAsync() {
        return activityDef.getParams().getOptionalInteger("async").orElse(1);
    }

    public synchronized SequenceBlocker getSequenceBlocker() {
        if (sequenceBlocker==null) {
            sequenceBlocker = new SequenceBlocker(getActivityDef().getStartCycle(), true);
        }
        return sequenceBlocker;
    }


    private final class DelayFunc_NoDelay implements LongUnaryOperator {
        @Override
        public long applyAsLong(long operand) {
            return 0L;
        }
    }

    private final class ResultFunc_Modulo128 implements LongToIntFunction {
        @Override
        public int applyAsInt(long value) {
            return (byte) (value%128);
        }
    }

    private final class ResultFunc_ResultModulo implements LongToIntFunction {

        private long modulo;

        private ResultFunc_ResultModulo(long modulo) {
            this.modulo = modulo;
        }

        @Override
        public int applyAsInt(long value) {
            if ((value%modulo)==0) {
                return 1;
            }
            return 0;
        }
    }

    private final class ResultFunc_StaticValue implements LongToIntFunction {

        private final long resultValue;

        public ResultFunc_StaticValue(long resultValue) {
            this.resultValue = resultValue;
        }
        @Override
        public int applyAsInt(long value) {
            return (int)resultValue;
        }
    }



}
