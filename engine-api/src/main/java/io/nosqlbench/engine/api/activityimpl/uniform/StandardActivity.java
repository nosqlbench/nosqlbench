package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.OpSource;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This is a typed activity which is expected to be come the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <O> A type of runnable which wraps the operations for this type of driver.
 */
public class StandardActivity<O extends Runnable> extends SimpleActivity {

    private final DriverAdapter<O> adapter;
    private final OpSource<O> opsource;

    public StandardActivity(DriverAdapter<O> adapter, ActivityDef activityDef) {
        super(activityDef);
        this.adapter = adapter;

        try {
            Function<ParsedCommand, OpDispenser<O>> opmapper = adapter.getOpMapper();
            Function<Map<String, Object>, Map<String, Object>> preprocessor = adapter.getPreprocessor();
            OpSequence<OpDispenser<O>> seq = createOpSourceFromCommands(opmapper,List.of(preprocessor));
            opsource= OpSource.of(seq);
        } catch (Exception e) {
            if (e instanceof OpConfigError) {
                throw e;
            } else {
                throw new OpConfigError("Error mapping workload template to operations: " + e.getMessage(), null, e);
            }
        }
    }

    public OpSource<O> getOpSource() {
        return opsource;
    }

//    public Function<OpTemplate, OpDispenser<? extends Runnable>> getRunnableOpFunction() {
//        DiagRunnableOpMapper mapper = new DiagRunnableOpMapper();
//        return mapper::apply;
//    }


}
