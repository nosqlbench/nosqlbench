package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class JMXAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(JMXAction.class);

    private final ActivityDef activityDef;
    private final int slot;
    private final JMXActivity activity;
    private OpSequence<LongFunction<JmxOp>> sequencer;

    public JMXAction(ActivityDef activityDef, int slot, JMXActivity activity) {
        this.activityDef = activityDef;
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        this.sequencer = activity.getSequencer();
    }

    @Override
    public int runCycle(long cycle) {
        LongFunction<JmxOp> readyJmxOp = sequencer.get(cycle);
        JmxOp jmxOp = readyJmxOp.apply(cycle);
        jmxOp.execute();
        return 0;
    }
}
