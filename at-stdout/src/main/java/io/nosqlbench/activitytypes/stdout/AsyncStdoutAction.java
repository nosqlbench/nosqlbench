package io.nosqlbench.activitytypes.stdout;

import com.codahale.metrics.Timer;
import io.nosqlbench.activityapi.core.BaseAsyncAction;
import io.nosqlbench.activityapi.core.ops.fluent.opfacets.StartedOp;
import io.nosqlbench.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.activityapi.planning.OpSequence;
import io.nosqlbench.activityimpl.ActivityDef;
import io.virtdata.templates.StringBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.LongFunction;

@SuppressWarnings("Duplicates")
public class AsyncStdoutAction extends BaseAsyncAction<StdoutOpContext, StdoutActivity> {
    private final static Logger logger = LoggerFactory.getLogger(AsyncStdoutAction.class);

    private OpSequence<StringBindings> sequencer;

    public AsyncStdoutAction(int slot, StdoutActivity activity) {
        super(activity, slot);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
        this.sequencer = activity.getOpSequence();
    }

    public StdoutOpContext allocateOpData(long cycle) {

        StdoutOpContext opc = new StdoutOpContext();
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            opc.stringBindings = sequencer.get(cycle);
            opc.statement = opc.stringBindings.bind(cycle);
            if (activity.getShowstmts()) {
                logger.info("STMT(cycle=" + cycle + "):\n" + opc.statement);
            }
        }
        return opc;
    }

    @Override
    public void startOpCycle(TrackedOp<StdoutOpContext> opc) {
        StartedOp<StdoutOpContext> started = opc.start();
        int result=0;
        try (Timer.Context executeTime = activity.executeTimer.time()) {
            activity.write(opc.getData().statement);
        } catch (Exception e) {
            result=1;
            started.fail(result);
            throw new RuntimeException("Error writing output:" + e, e);
        } finally {
            started.succeed(result);
        }
    }

    @Override
    public LongFunction<StdoutOpContext> getOpInitFunction() {
        return this::allocateOpData;
    }
}
