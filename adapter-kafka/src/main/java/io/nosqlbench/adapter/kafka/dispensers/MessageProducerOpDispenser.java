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

package io.nosqlbench.adapter.kafka.dispensers;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaClient;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaProducer;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashSet;

public class MessageProducerOpDispenser extends KafkaBaseOpDispenser {

    private static final Logger logger = LogManager.getLogger("MessageProducerOpDispenser");

    public static final String MSG_HEADER_OP_PARAM = "msg_header";
    public static final String MSG_KEY_OP_PARAM = "msg_key";
    public static final String MSG_BODY_OP_PARAM = "msg_body";

    private final Map<String, String> producerClientConfMap = new HashMap<>();

    protected final int txnBatchNum;
    private final LongFunction<String> msgHeaderJsonStrFunc;
    private final LongFunction<String> msgKeyStrFunc;
    private final LongFunction<String> msgValueStrFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> msgSeqErrSimuTypeSetFunc;

    public MessageProducerOpDispenser(final DriverAdapter adapter,
                                      final ParsedOp op,
                                      final LongFunction<String> tgtNameFunc,
                                      final KafkaSpace kafkaSpace) {
        super(adapter, op, tgtNameFunc, kafkaSpace);
        producerClientConfMap.putAll(kafkaSpace.getKafkaClientConf().getProducerConfMap());
        this.producerClientConfMap.put("bootstrap.servers", kafkaSpace.getBootstrapSvr());

        txnBatchNum = this.parsedOp.getStaticConfigOr("txn_batch_num", 0);

        msgHeaderJsonStrFunc = this.lookupOptionalStrOpValueFunc(MessageProducerOpDispenser.MSG_HEADER_OP_PARAM);
        msgKeyStrFunc = this.lookupOptionalStrOpValueFunc(MessageProducerOpDispenser.MSG_KEY_OP_PARAM);
        msgValueStrFunc = this.lookupMandtoryStrOpValueFunc(MessageProducerOpDispenser.MSG_BODY_OP_PARAM);

        msgSeqErrSimuTypeSetFunc = this.getStaticErrSimuTypeSetOpValueFunc();
        // Doc-level parameter: seq_tracking
        seqTrackingFunc = this.lookupStaticBoolConfigValueFunc(
            DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false);
    }

    private String getEffectiveClientId(final long cycle) {
        if (this.producerClientConfMap.containsKey("client.id")) {
            final String defaultClientIdPrefix = this.producerClientConfMap.get("client.id");
            final int clntIdx = (int) (cycle % this.kafkaClntCnt);

            return defaultClientIdPrefix + '-' + clntIdx;
        }
        return "";
    }

    private ProducerRecord<String, String> createKafkaMessage(
        final long curCycle,
        final String topicName,
        final String msgHeaderRawJsonStr,
        final String msgKey,
        final String msgValue
    ) {
        if (StringUtils.isAllBlank(msgKey, msgValue))
            throw new KafkaAdapterInvalidParamException("Message key and value can't both be empty!");

        int messageSize = KafkaAdapterUtil.getStrObjSize(msgKey) + KafkaAdapterUtil.getStrObjSize(msgValue);

        final ProducerRecord<String, String> record = new ProducerRecord<>(topicName, msgKey, msgValue);

        // Check if msgHeaderRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> msgHeaderProperties = new HashMap<>();
        if (!StringUtils.isBlank(msgHeaderRawJsonStr)) try {
            msgHeaderProperties = KafkaAdapterUtil.convertJsonToMap(msgHeaderRawJsonStr);
        } catch (final Exception e) {
            MessageProducerOpDispenser.logger.warn(
                "Error parsing message property JSON string {}, ignore message properties!",
                msgHeaderRawJsonStr);
        }

        for (final Entry<String, String> entry : msgHeaderProperties.entrySet()) {
            final String headerKey = entry.getKey();
            final String headerValue = entry.getValue();

            messageSize += KafkaAdapterUtil.getStrObjSize(headerKey) + KafkaAdapterUtil.getStrObjSize(headerValue);

            if (! StringUtils.isAnyBlank(headerKey, headerValue))
                record.headers().add(headerKey, headerValue.getBytes(StandardCharsets.UTF_8));

        }

        // NB-specific headers
        messageSize += KafkaAdapterUtil.getStrObjSize(KafkaAdapterUtil.NB_MSG_SEQ_PROP);
        messageSize += 8;
        messageSize += KafkaAdapterUtil.getStrObjSize(KafkaAdapterUtil.NB_MSG_SIZE_PROP);
        messageSize += 6;

        record.headers().add(KafkaAdapterUtil.NB_MSG_SEQ_PROP, String.valueOf(curCycle).getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize).getBytes(StandardCharsets.UTF_8));

        return record;
    }

    public OpTimeTrackKafkaProducer getOpTimeTrackKafkaProducer(long cycle,
                                                                final String topicName,
                                                                final String clientId)
    {
        String producerName = "producer-" + cycle % this.kafkaClntCnt;
        KafkaSpace.ProducerCacheKey producerCacheKey =
            new KafkaSpace.ProducerCacheKey(producerName, topicName, clientId);

        return kafkaSpace.getOpTimeTrackKafkaProducer(producerCacheKey, () -> {
            final Properties producerConfProps = new Properties();
            producerConfProps.putAll(this.producerClientConfMap);

            if (StringUtils.isNotBlank(clientId)) {
                producerConfProps.put("client.id", clientId);
            } else {
                producerConfProps.remove("client.id");
            }

            // When transaction batch number is less than 2, it is treated effectively as no-transaction
            if (2 > txnBatchNum) {
                producerConfProps.remove("transactional.id");
            }

            String baseTransactId = "";
            boolean transactionEnabled = false;
            if (producerConfProps.containsKey("transactional.id")) {
                baseTransactId = producerConfProps.getProperty("transactional.id");
                if (StringUtils.isNotBlank(baseTransactId)) {
                    producerConfProps.put(
                        "transactional.id",
                        baseTransactId + '-' + (cycle % this.kafkaClntCnt));
                    transactionEnabled = StringUtils.isNotBlank(producerConfProps.getProperty("transactional.id"));
                }
            }

            final KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfProps);
            if (transactionEnabled) producer.initTransactions();

            if (MessageProducerOpDispenser.logger.isDebugEnabled())
                MessageProducerOpDispenser.logger.debug(
                    "Producer created: {} -- transactionEnabled: {}, clientId: {})",
                    producer,
                    transactionEnabled,
                    clientId);

            return new OpTimeTrackKafkaProducer(
                this.kafkaSpace,
                this.asyncAPI,
                transactionEnabled,
                this.txnBatchNum,
                this.seqTrackingFunc.apply(cycle),
                this.msgSeqErrSimuTypeSetFunc.apply(cycle),
                producer);
        });
    }

    @Override
    public KafkaOp apply(final long cycle) {
        final String topicName = this.topicNameStrFunc.apply(cycle);
        final String clientId = this.getEffectiveClientId(cycle);

        final OpTimeTrackKafkaClient opTimeTrackKafkaProducer =
            this.getOpTimeTrackKafkaProducer(cycle, topicName, clientId);

        final ProducerRecord<String, String> message = this.createKafkaMessage(
            cycle,
            topicName,
            this.msgHeaderJsonStrFunc.apply(cycle),
            this.msgKeyStrFunc.apply(cycle),
            this.msgValueStrFunc.apply(cycle)
        );

        return new KafkaOp(
            this.kafkaAdapterMetrics,
            this.kafkaSpace,
            opTimeTrackKafkaProducer,
            message);
    }

    protected LongFunction<Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> getStaticErrSimuTypeSetOpValueFunc() {
        final LongFunction<Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> setStringLongFunction;
        setStringLongFunction = l ->
            this.parsedOp.getOptionalStaticValue(DOC_LEVEL_PARAMS.SEQERR_SIMU.label, String.class)
                .filter(Predicate.not(String::isEmpty))
                .map(value -> {
                    Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE> set = new HashSet<>();

                    if (StringUtils.contains(value,',')) set = Arrays.stream(value.split(","))
                        .map(EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE::parseSimuType)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                    return set;
                }).orElse(Collections.emptySet());
        MessageProducerOpDispenser.logger.info("{}: {}", DOC_LEVEL_PARAMS.SEQERR_SIMU.label, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }
}
