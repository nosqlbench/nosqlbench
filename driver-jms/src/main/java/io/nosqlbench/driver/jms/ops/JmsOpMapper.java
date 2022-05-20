package io.nosqlbench.driver.jms.ops;

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


import io.nosqlbench.driver.jms.JmsActivity;
import io.nosqlbench.driver.jms.util.JmsHeaderLongFunc;

import javax.jms.Destination;
import java.util.Map;
import java.util.function.LongFunction;

public abstract class JmsOpMapper implements LongFunction<JmsOp> {
    protected final JmsActivity jmsActivity;
    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<Destination> jmsDestinationFunc;

    public JmsOpMapper(JmsActivity jmsActivity,
                       LongFunction<Boolean> asyncApiFunc,
                       LongFunction<Destination> jmsDestinationFunc)
    {
        this.jmsActivity = jmsActivity;
        this.asyncApiFunc = asyncApiFunc;
        this.jmsDestinationFunc = jmsDestinationFunc;
    }
}
