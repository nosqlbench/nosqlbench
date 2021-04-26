package io.nosqlbench.driver.cqld4;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.util.concurrent.TimeUnit;

public class Cqld4Action implements SyncAction, ActivityDefObserver {

    private final int slot;
    private final Cqld4Activity activity;

    private Timer bindTimer;
    private Timer executeTimer;
    private Timer resultTimer;
    private Timer resultSuccessTimer;
    private Histogram triesHisto;
    private int maxTries;


    public Cqld4Action(int slot, Cqld4Activity activity) {
        this.slot = slot;
        this.activity = activity;
    }


    @Override
    public void init() {
        this.bindTimer = activity.getInstrumentation().getOrCreateBindTimer();
        this.executeTimer = activity.getInstrumentation().getOrCreateExecuteTimer();
        this.resultTimer = activity.getInstrumentation().getOrCreateResultTimer();
        this.resultSuccessTimer = activity.getInstrumentation().getOrCreateResultSuccessTimer();
        this.triesHisto = activity.getInstrumentation().getOrCreateTriesHistogram();
    }

    @Override
    public int runCycle(long cycle) {

        Cqld4Op cql4op;
        try (Timer.Context ctx = bindTimer.time()) {
            OpDispenser<Cqld4Op> opDispenser = activity.getSequence().get(cycle);
            cql4op = opDispenser.apply(cycle);
        }

        int tries = 0;
        int result = 0;
        Exception error=null;
        while (tries < maxTries) {
            tries++;
            long startat = System.nanoTime();
            try {
                try (Timer.Context ctx = executeTimer.time()) {
                    cql4op.run();
                }
            }
            catch (Exception e) {
                error=e;
            } finally {
                long nanos = System.nanoTime() - startat;
                resultTimer.update(nanos, TimeUnit.NANOSECONDS);

                if (error==null) {
                    resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
                    break;
                } else {
                    ErrorDetail detail = activity.getErrorhandler().handleError(error,cycle,nanos);
                    result = detail.resultCode;
                    if (!detail.isRetryable()) {
                        break;
                    }
                }
            }
        }
        triesHisto.update(tries);
        return result;
    }


    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        this.maxTries = activity.getActivityDef().getParams().getOptionalInteger("maxtries").orElse(10);
    }
}
