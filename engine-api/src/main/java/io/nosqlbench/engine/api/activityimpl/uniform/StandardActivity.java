package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.DiagRunnableOpMapper;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;

import java.util.function.Function;

/**
 * This is a typed activity which is expected to be come the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <O> A type of runnable which wraps the operations for this type of driver.
 */
public abstract class StandardActivity<O extends Runnable> extends SimpleActivity {

    private OpSequence<OpDispenser<O>> sequencer;

    public StandardActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    public synchronized OpSequence<OpDispenser<O>> getSequencer() {
        if (this.sequencer == null) {
            Function<OpTemplate, OpDispenser<O>> dispenserMapper = getOpMapperFunction();
        }
        return sequencer;
    }

    protected abstract Function<OpTemplate, OpDispenser<O>> getOpMapperFunction();


    public Function<OpTemplate, OpDispenser<? extends Runnable>> getRunnableOpFunction() {
        DiagRunnableOpMapper mapper = new DiagRunnableOpMapper();
        return mapper::apply;
    }


}
