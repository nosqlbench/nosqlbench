package io.nosqlbench.adapter.cqls3;

import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = DriverAdapter.class, selector = "cqls3")
public class Cqls3DriverAdapter extends Cqld4DriverAdapter {
    private final static Logger logger = LogManager.getLogger(Cqls3DriverAdapter.class);

    public Cqls3DriverAdapter() {
        super();
    }

    @Override
    public OpMapper<Op> getOpMapper() {
        logger.warn("This version of NoSQLBench uses the ScyllaDB Java Driver version 3 for all CQL workloads. In this preview version, advanced testing features present in the cql, cqld3 and cqld4 drivers are being implemented. If you need those features, please use only the release artifacts.");
        return super.getOpMapper();
    }
}
