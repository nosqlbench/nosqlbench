package io.nosqlbench.adapter.jdbc;

import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "jdbc")
public class JDBCDriverAdapter extends BaseDriverAdapter<JDBCOp, JDBCSpace> {
    private final static Logger logger = LogManager.getLogger(JDBCDriverAdapter.class);

    @Override
    public OpMapper<JDBCOp> getOpMapper() {
        DriverSpaceCache<? extends JDBCSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new JDBCOpMapper(this, adapterConfig, spaceCache);
    }

    @Override
    public Function<String, ? extends JDBCSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new JDBCSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(JDBCSpace.getConfigModel());
    }
}
