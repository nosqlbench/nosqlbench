package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class BaseCqlStmtDispenser extends BaseOpDispenser<Cqld4CqlOp> {

    private final LongFunction<Statement> stmtFunc;
    private final int maxpages;
    private final Cqld4OpMetrics metrics = new Cqld4OpMetrics();
    private final LongFunction<CqlSession> sessionFunc;
    private final boolean isRetryReplace;

    public BaseCqlStmtDispenser(LongFunction<CqlSession> sessionFunc, ParsedOp op) {
        super(op);
        this.sessionFunc = sessionFunc;
        this.stmtFunc = this.getCommonStmtFunc(op);
        this.maxpages = op.getStaticConfigOr("maxpages",1);
        this.isRetryReplace = op.getStaticConfigOr("retryreplace",false);
    }

    public int getMaxPages() {
        return maxpages;
    }

    public boolean isRetryReplace() {
        return isRetryReplace;
    }

    public LongFunction<CqlSession> getSessionFunc() {
        return sessionFunc;
    }
    /**
     * Implement this method to define a statement function, considering only
     * the functionality that is specific to that statement type.
     * Do not implement decorators which apply to {@link Statement} as these are
     * applied uniformly internal to the logic of {@link #getStmtFunc()}.
     * @param op The parsed op template
     * @return A statement function
     */
    protected abstract LongFunction<Statement> getPartialStmtFunction(ParsedOp op);

    /**
     * All implementations of a CQL Statement Dispenser should be using the method
     * provided by this function. This ensures that {@link Statement}-level attributes
     * are handled uniformly and in one place.
     * @return A function which produces a statement, fully ready to execute, with all
     * cross-type attributes handled consistently.
     */
    public LongFunction<Statement> getStmtFunc() {
        return stmtFunc;
    }

    /**
     * Any {@link Statement}-level attributes need to be handled here.
     * This is the initializer for the {@link #getStmtFunc()}} accessor method.
     * This takes the base statement function and decorates it optionally with each
     * additional qualified modifier, short-circuiting those which are not specified.
     * This allows default behavior to take precedence as well as avoids unnecessary calling
     * overhead for implicit attributes.
     * @param op A parsed op template.
     * @return A function which is used to construct {@link Statement} objects, ready to run.
     * However, this method is hidden to ensure that it is used only as a one-time initializer
     * at construction time.
     */
    private LongFunction<Statement> getCommonStmtFunc(ParsedOp op) {
        LongFunction<Statement> partial = getPartialStmtFunction(op);
        partial = op.enhanceEnum(partial, "cl", DefaultConsistencyLevel.class, Statement::setConsistencyLevel);
        partial = op.enhanceEnum(partial, "scl", DefaultConsistencyLevel.class, Statement::setSerialConsistencyLevel);
        partial = op.enhance(partial, "idempotent", Boolean.class, Statement::setIdempotent);
        return partial;
    }


}
