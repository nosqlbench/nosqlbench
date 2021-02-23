package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.driver.pulsar.PulsarSpaceCache;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.scoping.ScopedSupplier;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ReadyPulsarOp implements LongFunction<PulsarOp> {

    private final CommandTemplate cmdTpl;
    private final LongFunction<PulsarOp> opFunc;
    private final PulsarSpaceCache pcache;

    // TODO: Add docs for the command template with respect to the OpTemplate

    public ReadyPulsarOp(OpTemplate opTemplate, PulsarSpaceCache pcache) {
        // TODO: Consider parsing map structures into equivalent binding representation
        this.cmdTpl = new CommandTemplate(opTemplate);
        this.pcache = pcache;
        if (cmdTpl.isDynamic("op_scope")) {
            throw new RuntimeException("op_scope must be static");
        }
        this.opFunc = resolve();

        ScopedSupplier scope = ScopedSupplier.valueOf(cmdTpl.getStaticOr("op_scope", "singleton"));
        Supplier<LongFunction<PulsarOp>> opSupplier = scope.supplier(this::resolve);
    }

    private LongFunction<PulsarOp> resolve() {

        if (cmdTpl.containsKey("topic_url")) {
            throw new RuntimeException("topic_url is not valid. Perhaps you mean topic_uri ?");
        }

        LongFunction<String> topic_uri_func;
        if (cmdTpl.containsKey("topic_uri")) {
            if (cmdTpl.containsAny("tenant", "namespace", "topic", "persistent")) {
                throw new RuntimeException("You may not specify topic_uri with any of the piece-wise components 'persistence','tenant','namespace','topic'.");
            } else if (cmdTpl.isStatic("topic_uri")) {
                topic_uri_func = (l) -> cmdTpl.getStatic("topic_uri");
            } else {
                topic_uri_func = (l) -> cmdTpl.getDynamic("topic_uri", l);
            }
        }
        else {
            if (cmdTpl.containsKey("topic")) {
                if (cmdTpl.isStaticOrUnsetSet("persistence", "tenant", "namespace", "topic")) {
                    String persistence = cmdTpl.getStaticOr("persistence", "persistent")
                        .replaceAll("true", "persistent");

                    String tenant = cmdTpl.getStaticOr("tenant", "public");
                    String namespace = cmdTpl.getStaticOr("namespace", "default");
                    String topic = cmdTpl.getStaticOr("topic", "");

                    String composited = persistence + "://" + tenant + "/" + namespace + "/" + topic;
                    topic_uri_func = (l) -> composited;
                } else { // some or all dynamic fields, composite into a single dynamic call
                    topic_uri_func = (l) ->
                        cmdTpl.getOr("persistent", l, "persistent").replaceAll("true", "persistent")
                            + "://" + cmdTpl.getOr("tenant", l, "public")
                            + "/" + cmdTpl.getOr("namespace", l, "default")
                            + "/" + cmdTpl.getOr("topic", l, "");
                }
            }
            else {
                topic_uri_func = null;
            }
        }

        // TODO: At the moment, only supports static "client"
        PulsarSpace clientSpace;
        if (cmdTpl.containsKey("client")) {
            if (cmdTpl.isDynamic("client")) {
                throw new RuntimeException("\"client\" can't be made dynamic!");
            } else {
                String client_name = cmdTpl.getStatic("client");
                clientSpace = pcache.getPulsarSpace(client_name);
            }
        } else {
            clientSpace = pcache.getPulsarSpace("default");
        }

        assert (clientSpace != null);
        String clientType = clientSpace.getPulsarClientConf().getPulsarClientType();

        // TODO: At the moment, only implements "Producer" functionality; add implementation for others later!
        if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.PRODUCER.toString()) ) {
            return resolveProducer(clientSpace, cmdTpl, topic_uri_func);/*
        } else if ( msgOperation.equalsIgnoreCase(PulsarActivityUtil.MSGOP_TYPES.CONSUMER.toString()) ) {
            return resolveConsumer(spaceFunc, cmdTpl, topic_uri_func);
        } else if ( msgOperation.equalsIgnoreCase(PulsarOpUtil.MSGOP_TYPES.READER.toString()) ) {
        } else if ( msgOperation.equalsIgnoreCase(PulsarOpUtil.MSGOP_TYPES.WSOKT_PRODUCER.toString()) ) {
        } else if ( msgOperation.equalsIgnoreCase(PulsarOpUtil.MSGOP_TYPES.MANAGED_LEDGER.toString()) ) {
        */
        } else {
            throw new RuntimeException("Unsupported Pulsar message operation type.");
        }
    }

    private LongFunction<PulsarOp> resolveProducer(
        PulsarSpace pulsarSpace,
        CommandTemplate cmdTpl,
        LongFunction<String> topic_uri_func
    ) {
        LongFunction<Producer<?>> producerFunc;

        if (cmdTpl.isStatic("producer-name")) {
            producerFunc = (l) -> pulsarSpace.getProducer(cmdTpl.getStatic("producer-name"),
                (topic_uri_func == null) ? null : topic_uri_func.apply(l));
        } else if (cmdTpl.isDynamic("producer-name")) {
            producerFunc = (l) -> pulsarSpace.getProducer(cmdTpl.getDynamic("producer-name", l),
                (topic_uri_func == null) ? null : topic_uri_func.apply(l));
        } else {
            producerFunc = (l) -> pulsarSpace.getProducer(null,
                (topic_uri_func == null) ? null : topic_uri_func.apply(l));
        }

        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg-key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg-key");
        } else if (cmdTpl.isDynamic("msg-key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg-key", l);
        } else {
            keyFunc = null;
        }

        LongFunction<String> valueFunc;
        if (cmdTpl.containsKey("msg-value")) {
            if (cmdTpl.isStatic("msg-value")) {
                valueFunc = (l) -> cmdTpl.getStatic("msg-value");
            } else if (cmdTpl.isDynamic("msg-value")) {
                valueFunc = (l) -> cmdTpl.getDynamic("msg-value", l);
            } else {
                valueFunc = null;
            }
        } else {
            throw new RuntimeException("\"msg-value\" field must be specified!");
        }

        return new PulsarProducerMapper(producerFunc, keyFunc, valueFunc, pulsarSpace, cmdTpl);
    }

    @Override
    public PulsarOp apply(long value) {
        PulsarOp op = opFunc.apply(value);
        return op;
    }
}
