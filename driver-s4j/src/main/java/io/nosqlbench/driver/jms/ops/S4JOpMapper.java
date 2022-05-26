package io.nosqlbench.driver.jms.ops;

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


import io.nosqlbench.driver.jms.S4JActivity;

import javax.jms.JMSContext;
import java.util.function.LongFunction;

public abstract class S4JOpMapper implements LongFunction<S4JOp> {
    protected final S4JActivity s4JActivity;
    protected final JMSContext jmsContext;
    protected final String s4jOpType;

    protected final LongFunction<Boolean> tempDestBoolFunc;
    protected final LongFunction<String> destTypeStrFunc;
    protected final LongFunction<String> destNameStrFunc;
    protected final LongFunction<Boolean> asyncAPIBoolFunc;

    public S4JOpMapper(S4JActivity s4JActivity,
                       String s4jOpType,
                       LongFunction<Boolean> tempDestBoolFunc,
                       LongFunction<String> destTypeStrFunc,
                       LongFunction<String> destNameStrFunc,
                       LongFunction<Boolean> asyncAPIBoolFunc)
    {
        this.s4JActivity = s4JActivity;
        this.jmsContext = s4JActivity.getJmsContext();

        this.s4jOpType = s4jOpType;
        this.tempDestBoolFunc = tempDestBoolFunc;
        this.destTypeStrFunc = destTypeStrFunc;
        this.destNameStrFunc = destNameStrFunc;
        this.asyncAPIBoolFunc = asyncAPIBoolFunc;
    }
}
