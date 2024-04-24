/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.pulsar.dispensers;

import com.codahale.metrics.Timer.Context;
import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class PulsarClientOpDispenser extends PulsarBaseOpDispenser {

    private static final Logger logger = LogManager.getLogger("PulsarClientOpDispenser");

    protected final PulsarClient pulsarClient;
    protected final Schema<?> pulsarSchema;

    protected final LongFunction<Boolean> useTransactFunc;
    // TODO: add support for "operation number per transaction"
    // protected final LongFunction<Integer> transactBatchNumFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<String> payloadRttFieldFunc;
    protected final LongFunction<Supplier<Transaction>> transactSupplierFunc;
    protected final LongFunction<Set<MSG_SEQ_ERROR_SIMU_TYPE>> msgSeqErrSimuTypeSetFunc;

    protected PulsarClientOpDispenser(final DriverAdapter adapter,
                                      final ParsedOp op,
                                      final LongFunction<String> tgtNameFunc,
                                      final PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        pulsarClient = pulsarSpace.getPulsarClient();
        pulsarSchema = pulsarSpace.getPulsarSchema();

        // Doc-level parameter: use_transaction
        useTransactFunc = this.lookupStaticBoolConfigValueFunc(
            DOC_LEVEL_PARAMS.USE_TRANSACTION.label, false);

        // TODO: add support for "operation number per transaction"
        // Doc-level parameter: transact_batch_num
        // this.transactBatchNumFunc = lookupStaticIntOpValueFunc(
        //    PulsarAdapterUtil.DOC_LEVEL_PARAMS.TRANSACT_BATCH_NUM.label, 1);

        // Doc-level parameter: seq_tracking
        seqTrackingFunc = this.lookupStaticBoolConfigValueFunc(
            DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false);

        // Doc-level parameter: payload-tracking-field
        payloadRttFieldFunc = l -> this.parsedOp.getStaticConfigOr(
            DOC_LEVEL_PARAMS.RTT_TRACKING_FIELD.label, "");

        transactSupplierFunc = l -> this.getTransactionSupplier();

        msgSeqErrSimuTypeSetFunc = this.getStaticErrSimuTypeSetOpValueFunc();
    }

    protected Supplier<Transaction> getTransactionSupplier() {
        return () -> {
            try (final Context time = this.pulsarAdapterMetrics.getCommitTransactionTimer().time() ){
                return this.pulsarClient
                    .newTransaction()
                    .build()
                    .get();
            } catch (Exception err) {
                if (PulsarClientOpDispenser.logger.isWarnEnabled())
                    PulsarClientOpDispenser.logger.warn("Error while starting a new transaction", err);
                throw new PulsarAdapterUnexpectedException(err);
            }
        };
    }

    protected LongFunction<Set<MSG_SEQ_ERROR_SIMU_TYPE>> getStaticErrSimuTypeSetOpValueFunc() {
        final LongFunction<Set<MSG_SEQ_ERROR_SIMU_TYPE>> setStringLongFunction;
        setStringLongFunction = l ->
            this.parsedOp.getOptionalStaticValue(DOC_LEVEL_PARAMS.SEQERR_SIMU.label, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<MSG_SEQ_ERROR_SIMU_TYPE> set = new HashSet<>();

                if (StringUtils.contains(value,',')) set = Arrays.stream(value.split(","))
                    .map(MSG_SEQ_ERROR_SIMU_TYPE::parseSimuType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

                return set;
            }).orElse(Collections.emptySet());
        PulsarClientOpDispenser.logger.info("{}: {}", DOC_LEVEL_PARAMS.SEQERR_SIMU.label, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }
}
