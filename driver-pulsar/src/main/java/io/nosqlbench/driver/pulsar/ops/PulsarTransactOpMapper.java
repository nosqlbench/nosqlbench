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

package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public abstract class PulsarTransactOpMapper extends PulsarOpMapper {
    protected final LongFunction<Boolean> useTransactionFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<Supplier<Transaction>> transactionSupplierFunc;

    public PulsarTransactOpMapper(CommandTemplate cmdTpl,
                                  PulsarSpace clientSpace,
                                  PulsarActivity pulsarActivity,
                                  LongFunction<Boolean> asyncApiFunc,
                                  LongFunction<Boolean> useTransactionFunc,
                                  LongFunction<Boolean> seqTrackingFunc,
                                  LongFunction<Supplier<Transaction>> transactionSupplierFunc)
    {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc);
        this.useTransactionFunc = useTransactionFunc;
        this.seqTrackingFunc = seqTrackingFunc;
        this.transactionSupplierFunc = transactionSupplierFunc;
    }
}
