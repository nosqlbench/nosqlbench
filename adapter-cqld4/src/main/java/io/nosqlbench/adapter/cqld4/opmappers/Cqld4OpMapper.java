package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

public class Cqld4OpMapper implements OpMapper<Cqld4BaseOp> {


    private final DriverSpaceCache<? extends Cqld4Space> cache;
    private final NBConfiguration cfg;

    public Cqld4OpMapper(NBConfiguration config, DriverSpaceCache<? extends Cqld4Space> cache) {
        this.cfg = config;
        this.cache = cache;
    }

    /**
     * Determine what type of op dispenser to use for a given parsed op template, and return a new instance
     * for it. Since the operations under the CQL driver 4.* do not follow a common type structure, we use the
     * base types in the NoSQLBench APIs and treat them somewhat more generically than with other drivers.
     *
     * @param cmd The {@link ParsedOp} which is the parsed version of the user-provided op template.
     *            This contains all the fields provided by the user, as well as explicit knowledge of
     *            which ones are static and dynamic.
     * @return An op dispenser for each provided op command
     */
    public OpDispenser<? extends Cqld4BaseOp> apply(ParsedOp cmd) {

        Cqld4Space cqld4Space = cache.get(cmd.getStaticConfigOr("space", "default"));
        CqlSession session = cqld4Space.getSession();

        CqlD4OpType cmdtype = cmd.getEnumFromFieldOr(CqlD4OpType.class, CqlD4OpType.cql, "type");

//        OpDispenser<Cqld4CqlOp> t = new CqlD4CqlOpMapper(session).apply(cmd);

        return switch (cmdtype) {
            case cql -> new CqlD4CqlOpMapper(session).apply(cmd);
            case gremlin -> new Cqld4GremlinOpMapper(session).apply(cmd);
        };
    }


}
