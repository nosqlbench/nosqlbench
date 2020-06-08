package io.nosqlbench.activitytype.cqld4.statements.binders;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Session;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;

import java.util.function.Function;

public enum CqlBinderTypes {

    direct_array(DirectArrayValuesBinder::new),
    unset_aware(UnsettableValuesBinder::new),
    diag_binder(DiagnosticPreparedBinder::new);

    private final Function<CqlSession, ValuesArrayBinder<PreparedStatement, Statement<?>>> mapper;

    CqlBinderTypes(Function<CqlSession,ValuesArrayBinder<PreparedStatement,Statement<?>>> mapper) {
        this.mapper = mapper;
    }

    public final static CqlBinderTypes DEFAULT = unset_aware;

    public ValuesArrayBinder<PreparedStatement,Statement<?>> get(CqlSession session) {
        return mapper.apply(session);
    }

}
