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

package io.nosqlbench.adapter.kafka.dispensers;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaClient;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaProducer;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
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

    private final static Logger logger = LogManager.getLogger("MessageProducerOpDispenser");

    public static final String MSG_HEADER_OP_PARAM = "msg_header";
    public static final String MSG_KEY_OP_PARAM = "msg_key";
    public static final String MSG_BODY_OP_PARAM = "msg_body";

    private final Map<String, String> producerClientConfMap = new HashMap<>();

    protected final int txnBatchNum;
    private final LongFunction<String> msgHeaderJsonStrFunc;
    private final LongFunction<String> msgKeyStrFunc;
    private final LongFunction<String> msgValueStrFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> msgSeqErrSimuTypeSetFunc;

    public MessageProducerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      KafkaSpace kafkaSpace) {
        super(adapter, op, tgtNameFunc, kafkaSpace);
        // Doc-level parameter: seq_tracking
        this.seqTrackingFunc = lookupStaticBoolConfigValueFunc(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false);
        this.msgSeqErrSimuTypeSetFunc = getStaticErrSimuTypeSetOpValueFunc();

        this.producerClientConfMap.putAll(kafkaSpace.getKafkaClientConf().getProducerConfMap());
        producerClientConfMap.put("bootstrap.servers", kafkaSpace.getBootstrapSvr());

        this.txnBatchNum = parsedOp.getStaticConfigOr("txn_batch_num", Integer.valueOf(0));

        this.msgHeaderJsonStrFunc = lookupOptionalStrOpValueFunc(MSG_HEADER_OP_PARAM);
        this.msgKeyStrFunc = lookupOptionalStrOpValueFunc(MSG_KEY_OP_PARAM);
        this.msgValueStrFunc = lookupMandtoryStrOpValueFunc(MSG_BODY_OP_PARAM);
    }

    private String getEffectiveClientId(long cycle) {
        if (producerClientConfMap.containsKey("client.id")) {
            String defaultClientIdPrefix = producerClientConfMap.get("client.id");
            int clntIdx = (int) (cycle % kafkaClntCnt);

            return defaultClientIdPrefix + "-" + clntIdx;
        }
        else {
            return "";
        }
    }

    private OpTimeTrackKafkaClient getOrCreateOpTimeTrackKafkaProducer(long cycle,
                                                                       String topicName,
                                                                       String clientId)
    {
        String cacheKey = KafkaAdapterUtil.buildCacheKey(
            "producer-" + String.valueOf(cycle % kafkaClntCnt), topicName);

        OpTimeTrackKafkaClient opTimeTrackKafkaClient = kafkaSpace.getOpTimeTrackKafkaClient(cacheKey);
        if (opTimeTrackKafkaClient == null) {
            Properties producerConfProps = new Properties();
            producerConfProps.putAll(producerClientConfMap);

            if (StringUtils.isNotBlank(clientId))
                producerConfProps.put("client.id", clientId);
            else
                producerConfProps.remove("client.id");

            // When transaction batch number is less than 2, it is treated effectively as no-transaction
            if (txnBatchNum < 2)
                producerConfProps.remove("transactional.id");

            String baseTransactId = "";
            boolean transactionEnabled = false;
            if (producerConfProps.containsKey("transactional.id")) {
                baseTransactId = producerConfProps.get("transactional.id").toString();
                producerConfProps.put("transactional.id", baseTransactId + "-" + cacheKey);
                transactionEnabled = StringUtils.isNotBlank(producerConfProps.get("transactional.id").toString());
            }

            KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfProps);
            if (transactionEnabled) {
                producer.initTransactions();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Producer created: {}/{} -- ({}, {}, {})",
                    cacheKey,
                    producer,
                    topicName,
                    transactionEnabled,
                    clientId);
            }

            opTimeTrackKafkaClient = new OpTimeTrackKafkaProducer(
                kafkaSpace,
                asyncAPI,
                transactionEnabled,
                txnBatchNum,
                seqTrackingFunc.apply(cycle),
                msgSeqErrSimuTypeSetFunc.apply(cycle),
                producer);
            kafkaSpace.addOpTimeTrackKafkaClient(cacheKey, opTimeTrackKafkaClient);
        }

        return opTimeTrackKafkaClient;
    }

    private ProducerRecord<String, String> createKafkaMessage(
        long curCycle,
        String topicName,
        String msgHeaderRawJsonStr,
        String msgKey,
        String msgValue
    ) {
        if (StringUtils.isAllBlank(msgKey, msgValue)) {
            throw new KafkaAdapterInvalidParamException("Message key and value can't both be empty!");
        }

        int messageSize = KafkaAdapterUtil.getStrObjSize(msgKey) + KafkaAdapterUtil.getStrObjSize(msgValue);

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, msgKey, msgValue);

        // Check if msgHeaderRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> msgHeaderProperties = new HashMap<>();
        if (!StringUtils.isBlank(msgHeaderRawJsonStr)) {
            try {
                msgHeaderProperties = KafkaAdapterUtil.convertJsonToMap(msgHeaderRawJsonStr);
            } catch (Exception e) {
                logger.warn(
                    "Error parsing message property JSON string {}, ignore message properties!",
                    msgHeaderRawJsonStr);
            }
        }

        for (Map.Entry<String, String> entry : msgHeaderProperties.entrySet()) {
            String headerKey = entry.getKey();
            String headerValue = entry.getValue();

            messageSize += KafkaAdapterUtil.getStrObjSize(headerKey) + KafkaAdapterUtil.getStrObjSize(headerValue);

            if (! StringUtils.isAnyBlank(headerKey, headerValue)) {
                record.headers().add(headerKey, headerValue.getBytes());
            }

        }

        // NB-specific headers
        messageSize += KafkaAdapterUtil.getStrObjSize(KafkaAdapterUtil.NB_MSG_SEQ_PROP);
        messageSize += 8;
        messageSize += KafkaAdapterUtil.getStrObjSize(KafkaAdapterUtil.NB_MSG_SIZE_PROP);
        messageSize += 6;

        record.headers().add(KafkaAdapterUtil.NB_MSG_SEQ_PROP, String.valueOf(curCycle).getBytes());
        record.headers().add(KafkaAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize).getBytes());

        return record;
    }

    @Override
    public KafkaOp apply(long cycle) {
        String topicName = topicNameStrFunc.apply(cycle);
        String clientId = getEffectiveClientId(cycle);

        OpTimeTrackKafkaClient opTimeTrackKafkaProducer =
            getOrCreateOpTimeTrackKafkaProducer(cycle, topicName, clientId);

        ProducerRecord<String, String> message = createKafkaMessage(
            cycle,
            topicName,
            msgHeaderJsonStrFunc.apply(cycle),
            msgKeyStrFunc.apply(cycle),
            msgValueStrFunc.apply(cycle)
        );

        return new KafkaOp(
            kafkaAdapterMetrics,
            kafkaSpace,
            opTimeTrackKafkaProducer,
            message);
    }

    protected LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> getStaticErrSimuTypeSetOpValueFunc() {
        LongFunction<Set<PulsarAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE>> setStringLongFunction;
        setStringLongFunction = (l) ->
            parsedOp.getOptionalStaticValue(PulsarAdapterUtil.DOC_LEVEL_PARAMS.SEQERR_SIMU.label, String.class)
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
        logger.info(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.SEQERR_SIMU.label + ": {}",
            setStringLongFunction.apply(0));
        return setStringLongFunction;
    }
}
