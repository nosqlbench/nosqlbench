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

package io.nosqlbench.adapter.pulsar.dispensers;

import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class PulsarClientOpDispenser extends PulsarBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("PulsarClientOpDispenser");

    protected final PulsarClient pulsarClient;
    protected final Schema<?> pulsarSchema;

    protected final LongFunction<Boolean> useTransactFunc;
    // TODO: add support for "operation number per transaction"
    // protected final LongFunction<Integer> transactBatchNumFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<String> payloadRttFieldFunc;
    protected final LongFunction<Supplier<Transaction>> transactSupplierFunc;
    protected final LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> msgSeqErrSimuTypeSetFunc;

    public PulsarClientOpDispenser(DriverAdapter adapter,
                                   ParsedOp op,
                                   LongFunction<String> tgtNameFunc,
                                   PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        this.pulsarClient = pulsarSpace.getPulsarClient();
        this.pulsarSchema = pulsarSpace.getPulsarSchema();

        // Doc-level parameter: use_transaction
        this.useTransactFunc = lookupStaticBoolConfigValueFunc(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label, false);

        // TODO: add support for "operation number per transaction"
        // Doc-level parameter: transact_batch_num
        // this.transactBatchNumFunc = lookupStaticIntOpValueFunc(
        //    PulsarAdapterUtil.DOC_LEVEL_PARAMS.TRANSACT_BATCH_NUM.label, 1);

        // Doc-level parameter: seq_tracking
        this.seqTrackingFunc = lookupStaticBoolConfigValueFunc(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false);

        // Doc-level parameter: payload-tracking-field
        this.payloadRttFieldFunc = (l) -> parsedOp.getStaticConfigOr(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.RTT_TRACKING_FIELD.label, "");

        this.transactSupplierFunc = (l) -> getTransactionSupplier();

        this.msgSeqErrSimuTypeSetFunc = getStaticErrSimuTypeSetOpValueFunc();
    }

    protected Supplier<Transaction> getTransactionSupplier() {
        return () -> {
            try (Timer.Context time = pulsarAdapterMetrics.getCommitTransactionTimer().time() ){
                return pulsarClient
                    .newTransaction()
                    .build()
                    .get();
            } catch (ExecutionException | InterruptedException err) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Error while starting a new transaction", err);
                }
                throw new RuntimeException(err);
            } catch (PulsarClientException err) {
                throw new RuntimeException("Transactions are not enabled on Pulsar Client, " +
                    "please set client.enableTransaction=true in your Pulsar Client configuration");
            }
        };
    }

    protected LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> getStaticErrSimuTypeSetOpValueFunc() {
        LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> setStringLongFunction;
        setStringLongFunction = (l) -> parsedOp.getOptionalStaticValue("seqerr_simu", String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE> set = new HashSet<>();

                if (StringUtils.contains(value,',')) {
                    set = Arrays.stream(value.split(","))
                        .map(PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE::parseSimuType)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                }

                return set;
            }).orElse(Collections.emptySet());
        logger.info("seqerr_simu: {}", setStringLongFunction.apply(0));
        return setStringLongFunction;
    }
}
