package io.nosqlbench.activitytype.cqld4.statements.binders;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Session;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;

import java.util.function.Function;

public enum CqlBinderTypes {
    direct_array(s -> new DirectArrayValuesBinder()),
    unset_aware(UnsettableValuesBinder::new),
    diagnostic(s -> new DiagnosticPreparedBinder());

    private final Function<Session, ValuesArrayBinder<PreparedStatement, Statement<?>>> mapper;

    CqlBinderTypes(Function<Session,ValuesArrayBinder<PreparedStatement,Statement<?>>> mapper) {
        this.mapper = mapper;
    }

    public final static CqlBinderTypes DEFAULT = unset_aware;

    public ValuesArrayBinder<PreparedStatement,Statement<?>> get(Session session) {
        return mapper.apply(session);
    }

}
