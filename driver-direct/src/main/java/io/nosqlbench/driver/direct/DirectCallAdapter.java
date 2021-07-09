package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * This activity type driver allows you to dynamically map any available
 * Java API which is exposed to the NoSQLBench runtime, executing methods
 * on this API by name, (optionally) storing named results, and re-using
 * these named results as arguments to subsequent calls.
 *
 * It supports static method dispatch, instance methods, and per-thread
 * object scoping.
 */
@Service(value = DriverAdapter.class, selector = "directapi")
public class DirectCallAdapter extends BaseDriverAdapter<DirectCall,Void> {

    @Override
    public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
        return List.of(new DirectCallStmtParser());
    }

    @Override
    public OpMapper<DirectCall> getOpMapper() {
        return new DirectOpMapper();
    }
}
