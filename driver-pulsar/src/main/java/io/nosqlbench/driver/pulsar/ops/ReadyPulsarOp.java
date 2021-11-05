package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.*;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverParamException;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverUnsupportedOpException;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ReadyPulsarOp implements OpDispenser<PulsarOp> {

    private final static Logger logger = LogManager.getLogger(ReadyPulsarOp.class);

    private final OpTemplate opTpl;
    private final CommandTemplate cmdTpl;
    private final PulsarSpace clientSpace;
    private final LongFunction<PulsarOp> opFunc;
    private final PulsarActivity pulsarActivity;

    // TODO: Add docs for the command template with respect to the OpTemplate

    public ReadyPulsarOp(OpTemplate opTemplate, PulsarSpaceCache pcache, PulsarActivity pulsarActivity) {
        // TODO: Consider parsing map structures into equivalent binding representation
        this.pulsarActivity = pulsarActivity;
        this.opTpl = opTemplate;
        this.cmdTpl = new CommandTemplate(opTemplate);

        if (cmdTpl.isDynamic("op_scope")) {
            throw new PulsarDriverParamException("\"op_scope\" parameter must be static");
        }

        // TODO: At the moment, only supports static "client"
        if (cmdTpl.containsKey("client")) {
            if (cmdTpl.isDynamic("client")) {
                throw new PulsarDriverParamException("\"client\" parameter can't be made dynamic!");
            } else {
                String client_name = cmdTpl.getStatic("client");
                this.clientSpace = pcache.getPulsarSpace(client_name);
            }
        } else {
            this.clientSpace = pcache.getPulsarSpace("default");
        }

        this.opFunc = resolve();
    }

    @Override
    public PulsarOp apply(long value) {
        return opFunc.apply(value);
    }

    private LongFunction<PulsarOp> resolve() {

        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new PulsarDriverParamException("[resolve()] \"optype\" parameter must be static and have a valid value!");
        }
        String stmtOpType = cmdTpl.getStatic("optype");

        if (cmdTpl.containsKey("topic_url")) {
            throw new PulsarDriverParamException("[resolve()] \"topic_url\" parameter is not valid. Perhaps you mean \"topic_uri\"?");
        }

        // Doc-level parameter: topic_uri
        LongFunction<String> topicUriFunc = (l) -> null;
        if (cmdTpl.containsKey(PulsarActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label)) {
            if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label)) {
                topicUriFunc = (l) -> cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label);
            } else {
                topicUriFunc = (l) -> cmdTpl.getDynamic(PulsarActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label, l);
            }
        }
        logger.info("topic_uri: {}", topicUriFunc.apply(0));

        // Doc-level parameter: async_api
        LongFunction<Boolean> asyncApiFunc = (l) -> false;
        if (cmdTpl.containsKey(PulsarActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
            if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
                boolean value = BooleanUtils.toBoolean(cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label));
                asyncApiFunc = (l) -> value;
            } else {
                    throw new PulsarDriverParamException("[resolve()] \"" + PulsarActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label + "\" parameter cannot be dynamic!");
            }
        }
        logger.info("async_api: {}", asyncApiFunc.apply(0));

        // Doc-level parameter: async_api
        LongFunction<Boolean> useTransactionFunc = (l) -> false;
        if (cmdTpl.containsKey(PulsarActivityUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label)) {
            if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label)) {
                boolean value = BooleanUtils.toBoolean(cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label));
                useTransactionFunc = (l) -> value;
            } else {
                throw new PulsarDriverParamException("[resolve()] \"" + PulsarActivityUtil.DOC_LEVEL_PARAMS.USE_TRANSACTION.label + "\" parameter cannot be dynamic!");
            }
        }
        logger.info("use_transaction: {}", useTransactionFunc.apply(0));

        // Doc-level parameter: admin_delop
        LongFunction<Boolean> adminDelOpFunc = (l) -> false;
        if (cmdTpl.containsKey(PulsarActivityUtil.DOC_LEVEL_PARAMS.ADMIN_DELOP.label)) {
            if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.ADMIN_DELOP.label))
                adminDelOpFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.ADMIN_DELOP.label));
            else
                throw new PulsarDriverParamException("[resolve()] \"" + PulsarActivityUtil.DOC_LEVEL_PARAMS.ADMIN_DELOP.label + "\" parameter cannot be dynamic!");
        }
        logger.info("admin_delop: {}", adminDelOpFunc.apply(0));

        // Doc-level parameter: seq_tracking
        LongFunction<Boolean> seqTrackingFunc = (l) -> false;
        if (cmdTpl.containsKey(PulsarActivityUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label)) {
            if (cmdTpl.isStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label))
                seqTrackingFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(PulsarActivityUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label));
            else
                throw new PulsarDriverParamException("[resolve()] \"" + PulsarActivityUtil.DOC_LEVEL_PARAMS.SEQ_TRACKING.label + "\" parameter cannot be dynamic!");
        }
        logger.info("seq_tracking: {}", seqTrackingFunc.apply(0));

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
                false);
        }
        // Regular/non-admin operation: single message consuming from multiple-topics (consumer)
        else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_MULTI_CONSUME.label)) {
            return resolveMultiTopicMsgConsume(
                clientSpace,
                topicUriFunc,
                asyncApiFunc,
                useTransactionFunc,
                seqTrackingFunc);
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
                true);
        }
        // Invalid operation type
        else {
            throw new PulsarDriverUnsupportedOpException();
        }
    }

    // Admin API: create tenant
    private LongFunction<PulsarOp> resolveAdminTenant(
        PulsarSpace clientSpace,
        LongFunction<Boolean> asyncApiFunc,
        LongFunction<Boolean> adminDelOpFunc)
    {
        if ( cmdTpl.isDynamic("admin_roles") ||
             cmdTpl.isDynamic("allowed_clusters") ) {
            throw new PulsarDriverParamException("\"admin_roles\" or \"allowed_clusters\" parameter must NOT be dynamic!");
        }

        LongFunction<Set<String>> adminRolesFunc;
        Set<String> roleSet = new HashSet<>();
        if (cmdTpl.isStatic("admin_roles")) {
            // "admin_roles" includes comma-separated admin roles:
            // e.g. role1, role2
            String adminRolesStr = cmdTpl.getStatic("admin_roles");
            String[] roleArr = adminRolesStr.split(",");
            Set<String> stringSet = new HashSet<>(Arrays.asList(roleArr));
            roleSet.addAll(stringSet);
        }
        adminRolesFunc = (l) -> roleSet;

        LongFunction<Set<String>> allowedClustersFunc;
        Set<String> clusterSet = new HashSet<>();
        if (cmdTpl.isStatic("allowed_clusters")) {
            // "allowed_cluster" includes comma-separated cluster names:
            // e.g. cluster1, cluster2
            String allowedClustersStr = cmdTpl.getStatic("allowed_clusters");
            String[] clusterArr = allowedClustersStr.split(",");
            Set<String> stringSet = new HashSet<>(Arrays.asList(clusterArr));
            clusterSet.addAll(stringSet);
        }
        allowedClustersFunc = (l) -> clusterSet;

        LongFunction<String> tenantFunc;
        if (cmdTpl.isStatic("tenant")) {
            tenantFunc = (l) -> cmdTpl.getStatic("tenant");
        } else if (cmdTpl.isDynamic("tenant")) {
            tenantFunc = (l) -> cmdTpl.getDynamic("tenant", l);
        } else {
            tenantFunc = (l) -> null;
        }

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

    // Admin API: create tenant
    private LongFunction<PulsarOp> resolveAdminNamespace(
        PulsarSpace clientSpace,
        LongFunction<Boolean> asyncApiFunc,
        LongFunction<Boolean> adminDelOpFunc)
    {
        LongFunction<String> namespaceFunc;
        if (cmdTpl.isStatic("namespace")) {
            namespaceFunc = (l) -> cmdTpl.getStatic("namespace");
        } else if (cmdTpl.isDynamic("namespace")) {
            namespaceFunc = (l) -> cmdTpl.getDynamic("namespace", l);
        } else {
            namespaceFunc = (l) -> null;
        }

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
        LongFunction<String> enablePartionFunc = (l) -> null;
        if (cmdTpl.isStatic("enable_partition")) {
            enablePartionFunc = (l) -> cmdTpl.getStatic("enable_partition");
        } else if (cmdTpl.isDynamic("enable_partition")) {
            enablePartionFunc = (l) -> cmdTpl.getDynamic("enable_partition", l);
        }

        LongFunction<String> partitionNumFunc = (l) -> null;
        if (cmdTpl.isStatic("partition_num")) {
            partitionNumFunc = (l) -> cmdTpl.getStatic("partition_num");
        } else if (cmdTpl.isDynamic("partition_num")) {
            partitionNumFunc = (l) -> cmdTpl.getDynamic("partition_num", l);
        }

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

        LongFunction<String> cycle_producer_name_func;
        if (cmdTpl.isStatic("producer_name")) {
            cycle_producer_name_func = (l) -> cmdTpl.getStatic("producer_name");
        } else if (cmdTpl.isDynamic("producer_name")) {
            cycle_producer_name_func = (l) -> cmdTpl.getDynamic("producer_name", l);
        } else {
            cycle_producer_name_func = (l) -> null;
        }

        LongFunction<Producer<?>> producerFunc =
            (l) -> clientSpace.getProducer(topic_uri_func.apply(l), cycle_producer_name_func.apply(l));

        // check if we're going to simulate producer message out-of-sequence error
        // - message ordering
        // - message loss
        Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> seqErrSimuTypes = Collections.emptySet();
        if (cmdTpl.containsKey("seqerr_simu")) {
            if (cmdTpl.isStatic("seqerr_simu")) {
                seqErrSimuTypes = parseSimulatedErrorTypes(cmdTpl.getStatic("seqerr_simu"));
            } else {
                throw new PulsarDriverParamException("[resolveMsgSend()] \"seqerr_simu\" parameter cannot be dynamic!");
            }
        }

        // message key
        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg_key");
        } else if (cmdTpl.isDynamic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg_key", l);
        } else {
            keyFunc = (l) -> null;
        }

        // message property
        LongFunction<String> propFunc;
        if (cmdTpl.isStatic("msg_property")) {
            propFunc = (l) -> cmdTpl.getStatic("msg_property");
        } else if (cmdTpl.isDynamic("msg_property")) {
            propFunc = (l) -> cmdTpl.getDynamic("msg_property", l);
        } else {
            propFunc = (l) -> null;
        }

        LongFunction<String> valueFunc;
        if (cmdTpl.containsKey("msg_value")) {
            if (cmdTpl.isStatic("msg_value")) {
                valueFunc = (l) -> cmdTpl.getStatic("msg_value");
            } else if (cmdTpl.isDynamic("msg_value")) {
                valueFunc = (l) -> cmdTpl.getDynamic("msg_value", l);
            } else {
                valueFunc = (l) -> null;
            }
        } else {
            throw new PulsarDriverParamException("[resolveMsgSend()] \"msg_value\" field must be specified!");
        }

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
            .collect(Collectors.toSet());
    }

    private LongFunction<PulsarOp> resolveMsgConsume(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func,
        LongFunction<Boolean> useTransactionFunc,
        LongFunction<Boolean> seqTrackingFunc,
        boolean e2eMsgProc
    ) {
        LongFunction<String> subscription_name_func;
        if (cmdTpl.isStatic("subscription_name")) {
            subscription_name_func = (l) -> cmdTpl.getStatic("subscription_name");
        } else if (cmdTpl.isDynamic("subscription_name")) {
            subscription_name_func = (l) -> cmdTpl.getDynamic("subscription_name", l);
        } else {
            subscription_name_func = (l) -> null;
        }

        LongFunction<String> subscription_type_func;
        if (cmdTpl.isStatic("subscription_type")) {
            subscription_type_func = (l) -> cmdTpl.getStatic("subscription_type");
        } else if (cmdTpl.isDynamic("subscription_type")) {
            subscription_type_func = (l) -> cmdTpl.getDynamic("subscription_type", l);
        } else {
            subscription_type_func = (l) -> null;
        }

        LongFunction<String> consumer_name_func;
        if (cmdTpl.isStatic("consumer_name")) {
            consumer_name_func = (l) -> cmdTpl.getStatic("consumer_name");
        } else if (cmdTpl.isDynamic("consumer_name")) {
            consumer_name_func = (l) -> cmdTpl.getDynamic("consumer_name", l);
        } else {
            consumer_name_func = (l) -> null;
        }

        LongFunction<Supplier<Transaction>> transactionSupplierFunc =
            (l) -> clientSpace.getTransactionSupplier(); //TODO make it dependant on current cycle?

        LongFunction<Consumer<?>> consumerFunc = (l) ->
            clientSpace.getConsumer(
                topic_uri_func.apply(l),
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
            consumerFunc,
            e2eMsgProc);
    }

    private LongFunction<PulsarOp> resolveMultiTopicMsgConsume(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func,
        LongFunction<Boolean> useTransactionFunc,
        LongFunction<Boolean> seqTrackingFunc
    ) {
        // Topic list (multi-topic)
        LongFunction<String> topic_names_func;
        if (cmdTpl.isStatic("topic_names")) {
            topic_names_func = (l) -> cmdTpl.getStatic("topic_names");
        } else if (cmdTpl.isDynamic("topic_names")) {
            topic_names_func = (l) -> cmdTpl.getDynamic("topic_names", l);
        } else {
            topic_names_func = (l) -> null;
        }

        // Topic pattern (multi-topic)
        LongFunction<String> topics_pattern_func;
        if (cmdTpl.isStatic("topics_pattern")) {
            topics_pattern_func = (l) -> cmdTpl.getStatic("topics_pattern");
        } else if (cmdTpl.isDynamic("topics_pattern")) {
            topics_pattern_func = (l) -> cmdTpl.getDynamic("topics_pattern", l);
        } else {
            topics_pattern_func = (l) -> null;
        }

        LongFunction<String> subscription_name_func;
        if (cmdTpl.isStatic("subscription_name")) {
            subscription_name_func = (l) -> cmdTpl.getStatic("subscription_name");
        } else if (cmdTpl.isDynamic("subscription_name")) {
            subscription_name_func = (l) -> cmdTpl.getDynamic("subscription_name", l);
        } else {
            subscription_name_func = (l) -> null;
        }

        LongFunction<String> subscription_type_func;
        if (cmdTpl.isStatic("subscription_type")) {
            subscription_type_func = (l) -> cmdTpl.getStatic("subscription_type");
        } else if (cmdTpl.isDynamic("subscription_type")) {
            subscription_type_func = (l) -> cmdTpl.getDynamic("subscription_type", l);
        } else {
            subscription_type_func = (l) -> null;
        }

        LongFunction<String> consumer_name_func;
        if (cmdTpl.isStatic("consumer_name")) {
            consumer_name_func = (l) -> cmdTpl.getStatic("consumer_name");
        } else if (cmdTpl.isDynamic("consumer_name")) {
            consumer_name_func = (l) -> cmdTpl.getDynamic("consumer_name", l);
        } else {
            consumer_name_func = (l) -> null;
        }

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
            false);
    }

    private LongFunction<PulsarOp> resolveMsgRead(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func
    ) {
        LongFunction<String> reader_name_func;
        if (cmdTpl.isStatic("reader_name")) {
            reader_name_func = (l) -> cmdTpl.getStatic("reader_name");
        } else if (cmdTpl.isDynamic("reader_name")) {
            reader_name_func = (l) -> cmdTpl.getDynamic("reader_name", l);
        } else {
            reader_name_func = (l) -> null;
        }

        LongFunction<String> start_msg_pos_str_func;
        if (cmdTpl.isStatic("start_msg_position")) {
            start_msg_pos_str_func = (l) -> cmdTpl.getStatic("start_msg_position");
        } else if (cmdTpl.isDynamic("start_msg_position")) {
            start_msg_pos_str_func = (l) -> cmdTpl.getDynamic("start_msg_position", l);
        } else {
            start_msg_pos_str_func = (l) -> null;
        }

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
        LongFunction<String> cycle_batch_producer_name_func;
        if (cmdTpl.isStatic("batch_producer_name")) {
            cycle_batch_producer_name_func = (l) -> cmdTpl.getStatic("batch_producer_name");
        } else if (cmdTpl.isDynamic("batch_producer_name")) {
            cycle_batch_producer_name_func = (l) -> cmdTpl.getDynamic("batch_producer_name", l);
        } else {
            cycle_batch_producer_name_func = (l) -> null;
        }

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
        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg_key");
        } else if (cmdTpl.isDynamic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg_key", l);
        } else {
            keyFunc = (l) -> null;
        }

        // message property
        LongFunction<String> propFunc;
        if (cmdTpl.isStatic("msg_property")) {
            propFunc = (l) -> cmdTpl.getStatic("msg_property");
        } else if (cmdTpl.isDynamic("msg_property")) {
            propFunc = (l) -> cmdTpl.getDynamic("msg_property", l);
        } else {
            propFunc = (l) -> null;
        }

        LongFunction<String> valueFunc;
        if (cmdTpl.containsKey("msg_value")) {
            if (cmdTpl.isStatic("msg_value")) {
                valueFunc = (l) -> cmdTpl.getStatic("msg_value");
            } else if (cmdTpl.isDynamic("msg_value")) {
                valueFunc = (l) -> cmdTpl.getDynamic("msg_value", l);
            } else {
                valueFunc = (l) -> null;
            }
        } else {
            throw new PulsarDriverParamException("[resolveMsgBatchSend()] \"msg_value\" field must be specified!");
        }

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
