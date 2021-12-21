package io.nosqlbench.engine.api.activityimpl;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.concurrent.TimeUnit;

/**
 * {@inheritDoc}
 * See {@link OpDispenser} for details on how to use this type.
 *
 * Some details are tracked per op template, which aligns to the life-cycle of the op dispenser.
 * Thus, each op dispenser is where the stats for all related operations are kept.
 * @param <T> The type of operation
 */
public abstract class BaseOpDispenser<T> implements OpDispenser<T> {

    private final String name;
    private boolean instrument;
    private Histogram resultSizeHistogram;
    private Timer successTimer;
    private Timer errorTimer;

    public BaseOpDispenser(OpTemplate optpl) {
        this.name = optpl.getName();
    }

    public BaseOpDispenser(ParsedOp op) {
        this.name = op.getName();
        configureInstrumentation(op);
    }

    public BaseOpDispenser(CommandTemplate cmdtpl) {
        this.name = cmdtpl.getName();
    }

    /**
     * {@inheritDoc}
     * @param value The cycle number which serves as the seed for any
     *              generated op fields to be bound into an operation.
     * @return
     */
    @Override
    public abstract T apply(long value);

    private void configureInstrumentation(ParsedOp optpl) {
        this.instrument = optpl.getStaticConfigOr("instrument", false);
        if (instrument) {
            this.successTimer = ActivityMetrics.timer(optpl.getStaticConfigOr("alias","UNKNOWN")+"-"+optpl.getName()+"--success");
            this.errorTimer = ActivityMetrics.timer(optpl.getStaticConfigOr("alias","UNKNOWN")+"-"+optpl.getName()+"--error");
            this.resultSizeHistogram = ActivityMetrics.histogram(optpl.getStaticConfigOr("alias","UNKNOWN")+"-"+optpl.getName()+ "--resultset-size");
        }
    }

    @Override
    public void onSuccess(long cycleValue, long nanoTime, long resultsize) {
        if (!instrument) {
            return;
        }
        successTimer.update(nanoTime, TimeUnit.NANOSECONDS);
        resultSizeHistogram.update(resultsize);
//        ThreadLocalNamedTimers.TL_INSTANCE.get().stop(stopTimers);
    }

    @Override
    public void onError(long cycleValue, long resultNanos, Throwable t) {
        errorTimer.update(resultNanos, TimeUnit.NANOSECONDS);
    }

}
