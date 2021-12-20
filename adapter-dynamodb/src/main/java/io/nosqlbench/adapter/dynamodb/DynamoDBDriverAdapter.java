package io.nosqlbench.adapter.dynamodb;

import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Stability;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "dynamodb", maturity = Stability.Experimental)
public class DynamoDBDriverAdapter extends BaseDriverAdapter<DynamoDBOp, DynamoDBSpace> {

    @Override
    public OpMapper<DynamoDBOp> getOpMapper() {
        DriverSpaceCache<? extends DynamoDBSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new DynamoDBOpMapper(adapterConfig, spaceCache);
    }

    @Override
    public Function<String, ? extends DynamoDBSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new DynamoDBSpace(s,cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return DynamoDBSpace.getConfigModel();
    }
}
