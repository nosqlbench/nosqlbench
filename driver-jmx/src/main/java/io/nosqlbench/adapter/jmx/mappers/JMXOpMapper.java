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

package io.nosqlbench.adapter.jmx.mappers;

import io.nosqlbench.adapter.jmx.JMXOpTypes;
import io.nosqlbench.adapter.jmx.JMXSpace;
import io.nosqlbench.adapter.jmx.dispensers.JMXExplainDispenser;
import io.nosqlbench.adapter.jmx.dispensers.JMXPrintDispenser;
import io.nosqlbench.adapter.jmx.dispensers.JMXReadDispenser;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.errors.OpConfigError;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.function.LongFunction;

public class JMXOpMapper implements OpMapper<Op> {

    private final DriverSpaceCache<? extends JMXSpace> jmxCache;

    public JMXOpMapper(DriverSpaceCache<? extends JMXSpace> jmxCache) {
        this.jmxCache = jmxCache;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {

        LongFunction<String> spaceNameFunc = op.getAsFunctionOr("space","default");
        LongFunction<JMXSpace> spaceFunc = l -> jmxCache.get(spaceNameFunc.apply(l));

        LongFunction<? extends String> nameFunction = op.getAsRequiredFunction("object");
        LongFunction<ObjectName> oNameFunc = n -> {
            try {
                String name = nameFunction.apply(n);
                return new ObjectName(name);
            } catch (MalformedObjectNameException e) {
                throw new OpConfigError("You must specify a valid object name for any JMX operation:" + e);
            }
        };

        TypeAndTarget<JMXOpTypes, String> optype = op.getTypeAndTarget(JMXOpTypes.class, String.class, "type", "target");
        return switch (optype.enumId) {
            case Read -> new JMXReadDispenser(spaceFunc, oNameFunc, op);
            case Print -> new JMXPrintDispenser(spaceFunc, oNameFunc, op);
            case Explain -> new JMXExplainDispenser(spaceFunc, oNameFunc, op);
        };
    }
}
