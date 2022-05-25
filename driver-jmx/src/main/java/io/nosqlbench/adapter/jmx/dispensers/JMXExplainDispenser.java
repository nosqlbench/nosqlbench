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
import io.nosqlbench.adapter.jmx.operations.JMXExplainOperation;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

import javax.management.ObjectName;
import java.util.function.LongFunction;

public class JMXExplainDispenser extends BaseOpDispenser<Op> {
    private final LongFunction<JMXSpace> spaceF;
    private final LongFunction<ObjectName> nameF;

    public JMXExplainDispenser(LongFunction<JMXSpace> spaceF, LongFunction<ObjectName> nameF, ParsedOp op) {
        super(op);
        this.spaceF =spaceF;
        this.nameF = nameF;
    }

    @Override
    public Op apply(long value) {
        return new JMXExplainOperation(
            spaceF.apply(value).getConnector(),
            nameF.apply(value)
        );
    }
}
