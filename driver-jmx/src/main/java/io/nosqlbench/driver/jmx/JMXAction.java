package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JMXAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(JMXAction.class);

    private final ActivityDef activityDef;
    private final int slot;
    private final JMXActivity activity;
    private OpSequence<ReadyJmxOp> sequencer;

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
    public int runCycle(long value) {
        ReadyJmxOp readyJmxOp = sequencer.get(value);
        JmxOp jmxOp = readyJmxOp.bind(value);
        jmxOp.execute();
        return 0;
    }
}
