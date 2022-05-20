package io.nosqlbench.activitytype.cql.statements.binders;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
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
