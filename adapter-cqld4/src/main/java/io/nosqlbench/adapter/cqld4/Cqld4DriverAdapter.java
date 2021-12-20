package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "cqld4")
public class Cqld4DriverAdapter extends BaseDriverAdapter<Cqld4Op, Cqld4Space> {

    @Override
    public OpMapper<Cqld4Op> getOpMapper() {
        DriverSpaceCache<? extends Cqld4Space> spaceCache = getSpaceCache();
        NBConfiguration config = getConfiguration();
        return new Cqld4OpMapper(config, spaceCache);
    }

    @Override
    public Function<String, ? extends Cqld4Space> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new Cqld4Space(s,cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return Cqld4Space.getConfigModel();
    }


}
