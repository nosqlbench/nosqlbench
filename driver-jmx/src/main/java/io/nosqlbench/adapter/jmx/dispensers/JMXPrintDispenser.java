/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.jmx.dispensers;

import io.nosqlbench.adapter.jmx.JMXSpace;
import io.nosqlbench.adapter.jmx.operations.JMXPrintOperation;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import javax.management.ObjectName;
import java.util.function.LongFunction;

public class JMXPrintDispenser extends BaseOpDispenser<Op> {
    private final LongFunction<JMXSpace> spaceF;
    private final LongFunction<ObjectName> nameF;
    private final LongFunction<String> readvarF;
    private final LongFunction<String> asTypeF;
    private final LongFunction<String> asNameF;
    private final LongFunction<SharedState.Scope> scopeF;

    public JMXPrintDispenser(LongFunction<JMXSpace> spaceF, LongFunction<ObjectName> nameF, ParsedOp op) {
        super(op);
        this.spaceF =spaceF;
        this.nameF = nameF;
        this.readvarF = op.getAsFunctionOr("readvar","Value");
        this.asTypeF = op.getAsFunctionOr("as_type","String");
        this.asNameF = op.getAsOptionalFunction("as_name", String.class).orElse(readvarF);
        this.scopeF = op.getAsOptionalEnumFunction("scope", SharedState.Scope.class).orElse(l -> SharedState.Scope.thread);
    }

    @Override
    public Op apply(long value) {
        return new JMXPrintOperation(
            spaceF.apply(value),
            nameF.apply(value),
            readvarF.apply(value),
            asTypeF.apply(value),
            asNameF.apply(value),
            scopeF.apply(value)
        );
    }
}
