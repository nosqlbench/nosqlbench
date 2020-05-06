package io.nosqlbench.activitytype.cqld4.statements.binders;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Session;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;

public enum CqlBinderTypes {
    direct_array,
    unset_aware,
    diagnostic;

    public final static CqlBinderTypes DEFAULT = unset_aware;

    public ValuesArrayBinder<PreparedStatement, Statement> get(Session session) {
        if (this==direct_array) {
            return new DirectArrayValuesBinder();
        } else if (this== unset_aware) {
            return new UnsettableValuesBinder(session);
        } else if (this==diagnostic) {
            return new DiagnosticPreparedBinder();
        } else {
            throw new RuntimeException("Impossible-ish statement branch");
        }
    }

}
