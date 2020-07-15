package io.nosqlbench.driver.jmx;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;

public class JMXActivity extends SimpleActivity implements Activity {

    private OpSequence<ReadyJmxOp> sequence;

    public JMXActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.sequence = createOpSequenceFromCommands(ReadyJmxOp::new);
    }

    public OpSequence<ReadyJmxOp> getSequencer() {
        return sequence;
    }
}
