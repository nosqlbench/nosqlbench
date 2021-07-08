package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4PreparedBatchOpDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4BatchStatementDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedOpDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4SimpleCqlStatementDispenser;
import io.nosqlbench.driver.cqld4.Cqld4Op;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.errors.BasicError;

public class Cqld4OpMapper implements OpMapper<Cqld4Op> {


    private final DriverSpaceCache<? extends Cqld4Space> cache;

    public Cqld4OpMapper(DriverSpaceCache<? extends Cqld4Space> cache) {
        this.cache = cache;
    }

    public OpDispenser<Cqld4Op> apply(ParsedCommand cmd) {

        // if session field = static string, else ...

        boolean prepared = cmd.getStaticValueOr("prepared",false);
        boolean batch = cmd.getStaticValueOr("boolean",false);

        if (cmd.isDefinedDynamic("session")) {
            throw new BasicError("This driver adapter does not support dynamic sessions.");
        }
        // If it did, we would use something like this instead...
        //        LongFunction<String> session = cmd.getAsFunctionOr("session", "default");

        Cqld4Space cqld4Space = cache.get(cmd.getStaticValueOr("session", "default"));
        CqlSession session = cqld4Space.getSession();

        if (prepared && batch) {
            return new CqlD4PreparedBatchOpDispenser(session,cmd);
        } else if (prepared) {
            return new Cqld4PreparedOpDispenser(session,cmd);
        } else if (batch) {
            return new Cqld4BatchStatementDispenser(session, cmd);
        } else {
            return new Cqld4SimpleCqlStatementDispenser(session,cmd);
        }
    }

}
