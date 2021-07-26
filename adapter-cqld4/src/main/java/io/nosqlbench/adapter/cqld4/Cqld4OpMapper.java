package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4PreparedBatchOpDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4BatchStatementDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedStmtDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4SimpleCqlStmtDispenser;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

public class Cqld4OpMapper implements OpMapper<Cqld4Op> {


    private final DriverSpaceCache<? extends Cqld4Space> cache;
    private final NBConfiguration cfg;

    public Cqld4OpMapper(NBConfiguration config, DriverSpaceCache<? extends Cqld4Space> cache) {
        this.cfg = config;
        this.cache = cache;
    }

    public OpDispenser<Cqld4Op> apply(ParsedCommand cmd) {

        Cqld4Space cqld4Space = cache.get(cmd.getStaticConfigOr("space", "default"));
        boolean prepared = cmd.getStaticConfigOr("prepared", true);
        boolean batch = cmd.getStaticConfigOr("boolean", false);
        CqlSession session = cqld4Space.getSession();

        if (prepared && batch) {
            return new CqlD4PreparedBatchOpDispenser(session, cmd, cfg);
        } else if (prepared) {
            return new Cqld4PreparedStmtDispenser(session, cmd);
        } else if (batch) {
            return new Cqld4BatchStatementDispenser(session, cmd, cfg);
        } else {
            return new Cqld4SimpleCqlStmtDispenser(session, cmd, cfg);
        }
    }

}
