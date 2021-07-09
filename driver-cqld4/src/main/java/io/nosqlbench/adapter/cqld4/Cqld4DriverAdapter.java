package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Function;

@Service(value = DriverAdapter.class,selector = "cqld4")
public class Cqld4DriverAdapter extends BaseDriverAdapter<Cqld4Op,Cqld4Space> {

    private Cqld4SpaceCache sessionCache;

    @Override
    public OpMapper<Cqld4Op> getOpMapper() {
        return new Cqld4OpMapper(getSpaceCache());
    }

    @Override
    public Function<String, ? extends Cqld4Space> getSpaceInitializer() {
        return s -> new Cqld4Space(this);
    }
}
