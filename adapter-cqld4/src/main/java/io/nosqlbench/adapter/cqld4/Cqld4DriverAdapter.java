package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;

import java.util.function.Function;

@Service(value = DriverAdapter.class,selector = "cqld4")
public class Cqld4DriverAdapter extends BaseDriverAdapter<Cqld4Op,Cqld4Space> {

    @Override
    public OpMapper<Cqld4Op> getOpMapper() {
        return new Cqld4OpMapper(getSpaceCache());
    }

    @Override
    public Function<String, ? extends Cqld4Space> getSpaceInitializer() {
        return s -> new Cqld4Space(this);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(Cqld4DriverAdapter.class).asReadOnly();
    }


}
