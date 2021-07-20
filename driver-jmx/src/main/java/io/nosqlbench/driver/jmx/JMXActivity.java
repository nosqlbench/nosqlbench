package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.util.SSLKsFactory;

import javax.net.ssl.SSLContext;

public class JMXActivity extends SimpleActivity implements Activity {

    private OpSequence<OpDispenser<JmxOp>> sequence;
    private SSLContext sslContext;

    public JMXActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.sequence = createOpSequenceFromCommands(ReadyJmxOp::new);
        setDefaultsFromOpSequence(sequence);
        this.sslContext= SSLKsFactory.get().getContext(activityDef.getParams());

        // TODO: Require qualified default with an op sequence as the input
    }

    /**
     * If this is null, then no SSL is requested.
     * @return The SSLContext for this activity
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    public OpSequence<OpDispenser<JmxOp>> getSequencer() {
        return sequence;
    }
}
