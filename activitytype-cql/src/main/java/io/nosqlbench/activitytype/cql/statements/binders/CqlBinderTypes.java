package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.api.ValuesArrayBinder;

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
