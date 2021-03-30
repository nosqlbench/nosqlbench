package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.*;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.scoping.ScopedSupplier;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Reader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ReadyPulsarOp implements OpDispenser<PulsarOp> {

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
            throw new RuntimeException("op_scope must be static");
        }

        // TODO: At the moment, only supports static "client"
        if (cmdTpl.containsKey("client")) {
            if (cmdTpl.isDynamic("client")) {
                throw new RuntimeException("\"client\" can't be made dynamic!");
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

    private boolean isBoolean(String str) {
        return StringUtils.equalsAnyIgnoreCase(str, "yes", "true");
    }

    private LongFunction<PulsarOp> resolve() {

        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new RuntimeException("Statement parameter \"optype\" must have a valid value!");
        }
        String stmtOpType = cmdTpl.getStatic("optype");

        if (cmdTpl.containsKey("topic_url")) {
            throw new RuntimeException("topic_url is not valid. Perhaps you mean topic_uri ?");
        }

        // Global parameter: topic_uri (applies only to non-Admin API)
        LongFunction<String> topicUriFunc = (l) -> null;
        // Global parameter: async_api (applies only to non-Admin API)
        LongFunction<Boolean> asyncApiFunc = (l) -> false;

        if (!StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.ADMIN.label)) {
            if (cmdTpl.containsKey("topic_uri")) {
                if (cmdTpl.containsAny("tenant", "namespace", "topic", "persistent")) {
                    throw new RuntimeException("You may not specify topic_uri with any of the piece-wise components 'persistence','tenant','namespace','topic'.");
                } else if (cmdTpl.isStatic("topic_uri")) {
                    topicUriFunc = (l) -> cmdTpl.getStatic("topic_uri");
                } else {
                    topicUriFunc = (l) -> cmdTpl.getDynamic("topic_uri", l);
                }
            } else if (cmdTpl.containsKey("topic")) {
                if (cmdTpl.isStaticOrUnsetSet("persistence", "tenant", "namespace", "topic")) {
                    String persistence = cmdTpl.getStaticOr("persistence", "persistent")
                        .replaceAll("true", "persistent");

                    String tenant = cmdTpl.getStaticOr("tenant", "public");
                    String namespace = cmdTpl.getStaticOr("namespace", "default");
                    String topic = cmdTpl.getStaticOr("topic", "");

                    String composited = persistence + "://" + tenant + "/" + namespace + "/" + topic;
                    topicUriFunc = (l) -> composited;
                } else { // some or all dynamic fields, composite into a single dynamic call
                    topicUriFunc = (l) ->
                        cmdTpl.getOr("persistent", l, "persistent").replaceAll("true", "persistent")
                            + "://" + cmdTpl.getOr("tenant", l, "public")
                            + "/" + cmdTpl.getOr("namespace", l, "default")
                            + "/" + cmdTpl.getOr("topic", l, "");
                }
            }

            if (cmdTpl.containsKey("async_api")) {
                if (cmdTpl.isStatic("async_api"))
                    asyncApiFunc = (l) -> isBoolean(cmdTpl.getStatic("async_api"));
                else
                    throw new RuntimeException("\"async_api\" parameter cannot be dynamic!");
            } else {
                asyncApiFunc = (l) -> false;
            }
        }

        // TODO: Complete implementation for websocket-producer and managed-ledger
        if ( StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.ADMIN.label) ) {
            return resolveAdminRequest(clientSpace);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(clientSpace, topicUriFunc, asyncApiFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_CONSUME.label)) {
            return resolveMsgConsume(clientSpace, topicUriFunc, asyncApiFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.MSG_READ.label)) {
            return resolveMsgRead(clientSpace, topicUriFunc, asyncApiFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND_START.label)) {
            return resolveMsgBatchSendStart(clientSpace, topicUriFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND.label)) {
            return resolveMsgBatchSend(clientSpace);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarActivityUtil.OP_TYPES.BATCH_MSG_SEND_END.label)) {
            return resolveMsgBatchSendEnd(clientSpace);
        } else {
            throw new RuntimeException("Unsupported Pulsar operation type");
        }
    }

    private LongFunction<PulsarOp> resolveAdminRequest(PulsarSpace clientSpace) {
        if ( cmdTpl.isDynamic("admin_roles") ||
             cmdTpl.isDynamic("allowed_clusters") ) {
            throw new RuntimeException("\"admin_roles\" or \"allowed_clusters\" parameter must NOT be dynamic!");
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

        LongFunction<String> namespaceFunc;
        if (cmdTpl.isStatic("namespace")) {
            namespaceFunc = (l) -> cmdTpl.getStatic("namespace");
        } else if (cmdTpl.isDynamic("namespace")) {
            namespaceFunc = (l) -> cmdTpl.getDynamic("namespace", l);
        } else {
            namespaceFunc = (l) -> null;
        }

        return new PulsarAdminMapper(
            cmdTpl,
            clientSpace,
            adminRolesFunc,
            allowedClustersFunc,
            tenantFunc,
            namespaceFunc);
    }

    private LongFunction<PulsarOp> resolveMsgSend(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func
    ) {
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

        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg_key");
        } else if (cmdTpl.isDynamic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg_key", l);
        } else {
            keyFunc = (l) -> null;
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
            throw new RuntimeException("Producer:: \"msg_value\" field must be specified!");
        }

        return new PulsarProducerMapper(
            cmdTpl,
            clientSpace,
            producerFunc,
            async_api_func,
            keyFunc,
            valueFunc,
            pulsarActivity.getBytesCounter(),
            pulsarActivity.getMessagesizeHistogram());
    }

    private LongFunction<PulsarOp> resolveMsgConsume(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func,
        LongFunction<Boolean> async_api_func
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

        LongFunction<Consumer<?>> consumerFunc = (l) ->
            clientSpace.getConsumer(
                topic_uri_func.apply(l),
                topic_names_func.apply(l),
                topics_pattern_func.apply(l),
                subscription_name_func.apply(l),
                subscription_type_func.apply(l),
                consumer_name_func.apply(l)
            );

        return new PulsarConsumerMapper(cmdTpl, clientSpace, consumerFunc, async_api_func,
            pulsarActivity.getBytesCounter(), pulsarActivity.getMessagesizeHistogram());
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

        return new PulsarReaderMapper(cmdTpl, clientSpace, readerFunc, async_api_func);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSendStart(
        PulsarSpace clientSpace,
        LongFunction<String> topic_uri_func
    ) {
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

        return new PulsarBatchProducerStartMapper(cmdTpl, clientSpace, batchProducerFunc);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSend(PulsarSpace clientSpace) {
        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg_key");
        } else if (cmdTpl.isDynamic("msg_key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg_key", l);
        } else {
            keyFunc = (l) -> null;
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
            throw new RuntimeException("Batch Producer:: \"msg_value\" field must be specified!");
        }

        return new PulsarBatchProducerMapper(
            cmdTpl,
            clientSpace,
            keyFunc,
            valueFunc);
    }

    private LongFunction<PulsarOp> resolveMsgBatchSendEnd(PulsarSpace clientSpace) {
        return new PulsarBatchProducerEndMapper(cmdTpl, clientSpace);
    }
}
