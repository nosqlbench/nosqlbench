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
import io.nosqlbench.driver.pulsar.PulsarSpaceCache;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverParamException;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverUnsupportedOpException;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReadyPulsarOp extends BaseOpDispenser<PulsarOp> {

    // TODO: Add this to the pulsar driver docs
    public static final String RTT_TRACKING_FIELD = "payload-tracking-field";
    private final static Logger logger = LogManager.getLogger(ReadyPulsarOp.class);

    private final OpTemplate opTpl;
    private final CommandTemplate cmdTpl;
    private final PulsarSpace clientSpace;
    private final LongFunction<PulsarOp> opFunc;
    private final PulsarActivity pulsarActivity;

    // TODO: Add docs for the command template with respect to the OpTemplate

    public ReadyPulsarOp(OpTemplate optpl, PulsarSpaceCache pcache, PulsarActivity pulsarActivity) {
        super(optpl);
        // TODO: Consider parsing map structures into equivalent binding representation
        this.pulsarActivity = pulsarActivity;
        this.opTpl = optpl;
        this.cmdTpl = new CommandTemplate(optpl);

        // TODO: At the moment, only supports static "client"
        String client_name = lookupStaticParameter("client", false, "default");
        this.clientSpace = pcache.getPulsarSpace(client_name);

        this.opFunc = resolve();
    }

    @Override
    public PulsarOp apply(long value) {
        return opFunc.apply(value);
    }

    private LongFunction<PulsarOp> resolve() {

        String stmtOpType = lookupStaticParameter("optype", true, null);

        if (cmdTpl.containsKey("topic_url")) {
            throw new PulsarDriverParamException("[resolve()] \"topic_url\" parameter is not valid. Perhaps you mean \"topic_uri\"?");
        }

        // Doc-level parameter: topic_uri
        LongFunction<String> topicUriFunc = lookupParameterFunc(PulsarActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label);
        logger.info("topic_uri: {}", topicUriFunc.apply(0));

        // Doc-level parameter: async_api
        boolean useAsyncApi = BooleanUtils.toBoolean(lookupStaticParameter(PulsarActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, false, "false"));
        LongFunction<Boolean> asyncApiFunc = (l) -> useAsyncApi;
        logger.info("async_api: {}", useAsyncApi);

        // Doc-level parameter: use_transaction
        boolean useTransaction = BooleanUtils.toBoolean(lookupStaticParameter(PulsarActivityUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label, false, "false"));
        LongFunction<Boolean> useTransactionFunc = (l) -> useTransaction;
        logger.info("use_transaction: {}", useTransaction);

        // Doc-level parameter: admin_delop
        boolean adminDelOp = BooleanUtils.toBoolean(lookupStaticParameter(PulsarActivityUtil.DOC_LEVEL_PARAMS.ADMIN_DELOP.label, false, "false"));
        LongFunction<Boolean> adminDelOpFunc = (l) -> adminDelOp;
        logger.info("admin_delop: {}", adminDelOp);

        // Doc-level parameter: seq_tracking
        boolean seqTracking = BooleanUtils.toBoolean(lookupStaticParameter(PulsarActivityUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false, "false"));
        LongFunction<Boolean> seqTrackingFunc = (l) -> seqTracking;
        logger.info("seq_tracking: {}", seqTracking);

        // TODO: Collapse this pattern into a simple version and flatten out all call sites
        LongFunction<String> payloadRttFieldFunc = lookupParameterFunc(RTT_TRACKING_FIELD, false, "");
        logger.info("payload_rtt_field_func: {}", payloadRttFieldFunc.apply(0));

        // TODO: Complete implementation for websocket-producer and managed-ledger
        // Admin operation: create/delete tenant
        if ( StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.ADMIN_TENANT.label) ) {
            return resolveAdminTenant(clientSpace, asyncApiFunc, adminDelOpFunc);
        }
        // Admin operation: create/delete namespace
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.ADMIN_NAMESPACE.label)) {
            return resolveAdminNamespace(clientSpace, asyncApiFunc, adminDelOpFunc);
        }
        // Admin operation: create/delete topic
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.ADMIN_TOPIC.label)) {
            return resolveAdminTopic(clientSpace, topicUriFunc, asyncApiFunc, adminDelOpFunc);
        }
        // Regular/non-admin operation: single message sending (producer)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(clientSpace, topicUriFunc, asyncApiFunc, useTransactionFunc, seqTrackingFunc);
        }
        // Regular/non-admin operation: single message consuming from a single topic (consumer)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_CONSUME.label)) {
            return resolveMsgConsume(
                clientSpace,
                topicUriFunc,
                asyncApiFunc,
                useTransactionFunc,
                seqTrackingFunc,
                parseEndToEndStartingTimeSourceParameter(EndToEndStartingTimeSource.NONE),
                payloadRttFieldFunc);
        }
        // Regular/non-admin operation: single message consuming from multiple-topics (consumer)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_MULTI_CONSUME.label)) {
            return resolveMultiTopicMsgConsume(
                clientSpace,
                topicUriFunc,
                asyncApiFunc,
                useTransactionFunc,
                seqTrackingFunc,
                payloadRttFieldFunc);
        }
        // Regular/non-admin operation: single message consuming a single topic (reader)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_READ.label)) {
            return resolveMsgRead(clientSpace, topicUriFunc, asyncApiFunc);
        }
        // Regular/non-admin operation: batch message processing - batch start
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND_START.label)) {
            return resolveMsgBatchSendStart(clientSpace, topicUriFunc, asyncApiFunc);
        }
        // Regular/non-admin operation: batch message processing - message sending (producer)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND.label)) {
            return resolveMsgBatchSend(clientSpace, asyncApiFunc);
        }
        // Regular/non-admin operation: batch message processing - batch send
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND_END.label)) {
            return resolveMsgBatchSendEnd(clientSpace, asyncApiFunc);
        }
        // Regular/non-admin operation: end-to-end message processing - sending message
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.E2E_MSG_PROC_SEND.label)) {
            return resolveMsgSend(clientSpace, topicUriFunc, asyncApiFunc, useTransactionFunc, seqTrackingFunc);
        }
        // Regular/non-admin operation: end-to-end message processing - consuming message
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.E2E_MSG_PROC_CONSUME.label)) {
            return resolveMsgConsume(
                clientSpace,
                topicUriFunc,
                asyncApiFunc,
                useTransactionFunc,
                seqTrackingFunc,
                parseEndToEndStartingTimeSourceParameter(
                    EndToEndStartingTimeSource.MESSAGE_PUBLISH_TIME),
                payloadRttFieldFunc);
        }
        // Invalid operation type
        else {
            throw new PulsarDriverUnsupportedOpException();
        }
    }

    private EndToEndStartingTimeSource parseEndToEndStartingTimeSourceParameter(EndToEndStartingTimeSource defaultValue) {
        EndToEndStartingTimeSource endToEndStartingTimeSource = defaultValue;
        if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.E2E_STARTING_TIME_SOURCE.label)) {
            endToEndStartingTimeSource = EndToEndStartingTimeSource.valueOf(cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.E2E_STARTING_TIME_SOURCE.label).toUpperCase());
        }
        return endToEndStartingTimeSource;
    }

    // Admin API: create tenant
    private LongFunction<PulsarOp> resolveAdminTenant(
        PulsarSpace clientSpace,
        LongFunction<Boolean> asyncApiFunc,
        LongFunction<Boolean> adminDelOpFunc)
    {
        // "admin_roles" includes comma-separated admin roles:
        // e.g. role1, role2
        Set<String> roleSet = lookupStaticParameterSet("admin_roles");
        LongFunction<Set<String>> adminRolesFunc = (l) -> roleSet;

        // "allowed_cluster" includes comma-separated cluster names:
        // e.g. cluster1, cluster2
        Set<String> clusterSet = lookupStaticParameterSet("allowed_clusters");
        LongFunction<Set<String>> allowedClustersFunc = (l) -> clusterSet;

        LongFunction<String> tenantFunc = lookupParameterFunc("tenant");

        return new PulsarAdminTenantMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc,
            adminDelOpFunc,
            adminRolesFunc,
            allowedClustersFunc,
            tenantFunc);
    }

    private Set<String> lookupStaticParameterSet(String parameterName) {
        return Optional.ofNullable(lookupStaticParameter(parameterName))
            .map(value -> {
                Set<String> set = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                return set;
            }).orElse(Collections.emptySet());
    }

    // Admin API: create tenant
    private LongFunction<PulsarOp> resolveAdminNamespace(
        PulsarSpace clientSpace,
        LongFunction<Boolean> asyncApiFunc,
        LongFunction<Boolean> adminDelOpFunc)
    {
        LongFunction<String> namespaceFunc = lookupParameterFunc("namespace");

        return new PulsarAdminNamespaceMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc,
            adminDelOpFunc,
            namespaceFunc);
    }

    // Admin API: create partitioned topic
    private LongFunction<PulsarOp> resolveAdminTopic(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_fun,
        LongFunction<Boolean> asyncApiFunc,
        LongFunction<Boolean> adminDelOpFunc
    ) {
        LongFunction<String> enablePartionFunc = lookupParameterFunc("enable_partition");

        LongFunction<String> partitionNumFunc = lookupParameterFunc("partition_num");

        return new PulsarAdminTopicMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc,
            adminDelOpFunc,
            topic_uri_fun,
            enablePartionFunc,
            partitionNumFunc);
    }

    private LongFunction<PulsarOp> resolveMsgSend(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func,
        LongFunction<Boolean> useTransactionFunc,
        LongFunction<Boolean> seqTrackingFunc
    ) {
        LongFunction<Supplier<Transaction>> transactionSupplierFunc =
            (l) -> clientSpace.getTransactionSupplier(); //TODO make it dependant on current cycle?

        LongFunction<String> cycle_producer_name_func = lookupParameterFunc("producer_name");

        LongFunction<Producer<?>> producerFunc =
            (l) -> clientSpace.getProducer(topic_uri_func.apply(l), cycle_producer_name_func.apply(l));

        // check if we're going to simulate producer message out-of-sequence error
        // - message ordering
        // - message loss
        Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> seqErrSimuTypes = parseSimulatedErrorTypes(lookupStaticParameter("seqerr_simu"));

        // message key
        LongFunction<String> keyFunc = lookupParameterFunc("msg_key");

        // message property
        LongFunction<String> propFunc = lookupParameterFunc("msg_property");

        LongFunction<String> valueFunc = lookupParameterFunc("msg_value", true);

        return new PulsarProducerMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            async_api_func,
            useTransactionFunc,
            seqTrackingFunc,
            transactionSupplierFunc,
            producerFunc,
            seqErrSimuTypes,
            keyFunc,
            propFunc,
            valueFunc);
    }

    private Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> parseSimulatedErrorTypes(String sequenceErrorSimulatedTypeString) {
        if (StringUtils.isBlank(sequenceErrorSimulatedTypeString)) {
            return Collections.emptySet();
        }
        return Arrays.stream(StringUtils.split(sequenceErrorSimulatedTypeString, ','))
            .map(PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE::parseSimuType)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LongFunction<PulsarOp> resolveMsgConsume(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func,
        LongFunction<Boolean> useTransactionFunc,
        LongFunction<Boolean> seqTrackingFunc,
        EndToEndStartingTimeSource endToEndStartingTimeSource,
        LongFunction<String> rttTrackingFieldFunc
    ) {
        LongFunction<String> subscription_name_func = lookupParameterFunc("subscription_name");

        LongFunction<String> subscription_type_func = lookupParameterFunc("subscription_type");

        LongFunction<String> consumer_name_func = lookupParameterFunc("consumer_name");

        LongFunction<String> ranges_func = lookupParameterFunc("ranges", false, "");

        LongFunction<Supplier<Transaction>> transactionSupplierFunc =
            (l) -> clientSpace.getTransactionSupplier(); //TODO make it dependant on current cycle?

        LongFunction<Consumer<?>> consumerFunc = (l) ->
            clientSpace.getConsumer(
                topic_uri_func.apply(l),
                subscription_name_func.apply(l),
                subscription_type_func.apply(l),
                consumer_name_func.apply(l),
                ranges_func.apply(l)
            );

        return new PulsarConsumerMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            async_api_func,
            useTransactionFunc,
            seqTrackingFunc,
            transactionSupplierFunc,
            consumerFunc,
            endToEndStartingTimeSource,
            rttTrackingFieldFunc);
    }

    private LongFunction<PulsarOp> resolveMultiTopicMsgConsume(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func,
        LongFunction<Boolean> useTransactionFunc,
        LongFunction<Boolean> seqTrackingFunc,
        LongFunction<String> payloadRttFieldFunc
    ) {
        // Topic list (multi-topic)
        LongFunction<String> topic_names_func = lookupParameterFunc("topic_names");

        // Topic pattern (multi-topic)
        LongFunction<String> topics_pattern_func = lookupParameterFunc("topics_pattern");

        LongFunction<String> subscription_name_func = lookupParameterFunc("subscription_name");

        LongFunction<String> subscription_type_func = lookupParameterFunc("subscription_type");

        LongFunction<String> consumer_name_func = lookupParameterFunc("consumer_name");

        LongFunction<Supplier<Transaction>> transactionSupplierFunc =
            (l) -> clientSpace.getTransactionSupplier(); //TODO make it dependant on current cycle?

        LongFunction<Consumer<?>> mtConsumerFunc = (l) ->
            clientSpace.getMultiTopicConsumer(
                topic_uri_func.apply(l),
                topic_names_func.apply(l),
                topics_pattern_func.apply(l),
                subscription_name_func.apply(l),
                subscription_type_func.apply(l),
                consumer_name_func.apply(l)
            );

        return new PulsarConsumerMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            async_api_func,
            useTransactionFunc,
            seqTrackingFunc,
            transactionSupplierFunc,
            mtConsumerFunc,
            parseEndToEndStartingTimeSourceParameter(EndToEndStartingTimeSource.NONE),
            payloadRttFieldFunc);
    }

    private LongFunction<String> lookupParameterFunc(String parameterName) {
        return lookupParameterFunc(parameterName, false, null);
    }

    private LongFunction<String> lookupParameterFunc(String parameterName, boolean required) {
        return lookupParameterFunc(parameterName, required, null);
    }

    private LongFunction<String> lookupParameterFunc(String parameterName, boolean required, String defaultValue) {
        if (cmdTpl.containsKey(parameterName)) {
            LongFunction<String> lookupFunc;
            if (cmdTpl.isStatic(parameterName)) {
                String staticValue = cmdTpl.getStatic(parameterName);
                lookupFunc = (l) -> staticValue;
            } else if (cmdTpl.isDynamic(parameterName)) {
                lookupFunc = (l) -> cmdTpl.getDynamic(parameterName, l);
            } else {
                lookupFunc = (l) -> defaultValue;
            }
            return lookupFunc;
        } else {
            if (required) {
                throw new PulsarDriverParamException("\"" + parameterName + "\" field must be specified!");
            } else {
                return (l) -> defaultValue;
            }
        }
    }

    private String lookupStaticParameter(String parameterName) {
        return lookupStaticParameter(parameterName, false, null);
    }

    private String lookupStaticParameter(String parameterName, boolean required, String defaultValue) {
        if (cmdTpl.containsKey(parameterName)) {
            if (cmdTpl.isStatic(parameterName)) {
                return cmdTpl.getStatic(parameterName);
            } else if (cmdTpl.isDynamic(parameterName)) {
                throw new PulsarDriverParamException("\"" + parameterName + "\" parameter must be static");
            } else {
                return defaultValue;
            }
        } else {
            if (required) {
                throw new PulsarDriverParamException("\"" + parameterName + "\" field must be specified!");
            } else {
                return defaultValue;
            }
        }
    }

    private LongFunction<Boolean> toBooleanFunc(LongFunction<String> parameterFunc) {
        return (l) -> BooleanUtils.toBoolean(parameterFunc.apply(l));
    }

    private LongFunction<PulsarOp> resolveMsgRead(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func
    ) {
        LongFunction<String> reader_name_func = lookupParameterFunc("reader_name");

        LongFunction<String> start_msg_pos_str_func = lookupParameterFunc("start_msg_position");

        LongFunction<Reader<?>> readerFunc = (l) ->
            clientSpace.getReader(
                topic_uri_func.apply(l),
                reader_name_func.apply(l),
                start_msg_pos_str_func.apply(l)
            );

        return new PulsarReaderMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            async_api_func,
            readerFunc);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSendStart(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> asyncApiFunc)
    {
        LongFunction<String> cycle_batch_producer_name_func = lookupParameterFunc("batch_producer_name");

        LongFunction<Producer<?>> batchProducerFunc =
            (l) -> clientSpace.getProducer(topic_uri_func.apply(l), cycle_batch_producer_name_func.apply(l));

        return new PulsarBatchProducerStartMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc,
            batchProducerFunc);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSend(PulsarSpace clientSpace,
                                                       LongFunction<Boolean> asyncApiFunc)
    {
        LongFunction<String> keyFunc = lookupParameterFunc("msg_key");

        // message property
        LongFunction<String> propFunc = lookupParameterFunc("msg_property");

        LongFunction<String> valueFunc = lookupParameterFunc("msg_value", true);

        return new PulsarBatchProducerMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc,
            keyFunc,
            propFunc,
            valueFunc);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSendEnd(PulsarSpace clientSpace,
                                                          LongFunction<Boolean> asyncApiFunc)
    {
        return new PulsarBatchProducerEndMapper(
            cmdTpl,
            clientSpace,
            pulsarActivity,
            asyncApiFunc);
    }
}
