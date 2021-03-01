package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.*;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.scoping.ScopedSupplier;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ReadyPulsarOp implements LongFunction<PulsarOp> {

    private final OpTemplate opTpl;
    private final CommandTemplate cmdTpl;
    private final PulsarSpace clientSpace;
    private final LongFunction<PulsarOp> opFunc;
    private final Schema<?> pulsarSchema;

    // TODO: Add docs for the command template with respect to the OpTemplate

    public ReadyPulsarOp(OpTemplate opTemplate, PulsarSpaceCache pcache) {
        // TODO: Consider parsing map structures into equivalent binding representation
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

        this.pulsarSchema = clientSpace.getPulsarSchema();

        this.opFunc = resolve();

        ScopedSupplier scope = ScopedSupplier.valueOf(cmdTpl.getStaticOr("op_scope", "singleton"));
        Supplier<LongFunction<PulsarOp>> opSupplier = scope.supplier(this::resolve);
    }

    private LongFunction<PulsarOp> resolve() {
        String clientType = clientSpace.getPulsarClientConf().getPulsarClientType();

        // TODO: Complete implementation for reader, websocket-producer and managed-ledger
        if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.PRODUCER.toString()) ) {
            assert clientSpace instanceof PulsarProducerSpace;
            return resolveProducer((PulsarProducerSpace) clientSpace);
        } else if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.CONSUMER.toString()) ) {
            assert clientSpace instanceof PulsarConsumerSpace;
            return resolveConsumer((PulsarConsumerSpace)clientSpace);
        } else if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.READER.toString()) ) {
            assert clientSpace instanceof PulsarReaderSpace;
            return resolveReader((PulsarReaderSpace)clientSpace); /*
        } else if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.WSOKT_PRODUCER.toString()) ) {
        } else if ( clientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.MANAGED_LEDGER.toString()) ) {
        */
        } else {
            throw new RuntimeException("Unsupported Pulsar client: " + clientType);
        }
    }

    private LongFunction<PulsarOp> resolveProducer(
        PulsarProducerSpace clientSpace
    ) {
        if (cmdTpl.containsKey("topic_url")) {
            throw new RuntimeException("topic_url is not valid. Perhaps you mean topic_uri ?");
        }

        LongFunction<String> cycle_producer_name_func;
        if (cmdTpl.isStatic("producer-name")) {
            cycle_producer_name_func = (l) -> cmdTpl.getStatic("producer-name");
        } else if (cmdTpl.isDynamic("producer-name")) {
            cycle_producer_name_func = (l) -> cmdTpl.getDynamic("producer-name", l);
        } else {
            cycle_producer_name_func = (l) -> null;
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
        else if (cmdTpl.containsKey("topic")) {
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
            topic_uri_func = (l) -> null;
        }

        LongFunction<Producer<?>> producerFunc =
            (l) -> clientSpace.getProducer(cycle_producer_name_func.apply(l), topic_uri_func.apply(l));

        LongFunction<String> keyFunc;
        if (cmdTpl.isStatic("msg-key")) {
            keyFunc = (l) -> cmdTpl.getStatic("msg-key");
        } else if (cmdTpl.isDynamic("msg-key")) {
            keyFunc = (l) -> cmdTpl.getDynamic("msg-key", l);
        } else {
            keyFunc = (l) -> null;
        }

        LongFunction<String> valueFunc;
        if (cmdTpl.containsKey("msg-value")) {
            if (cmdTpl.isStatic("msg-value")) {
                valueFunc = (l) -> cmdTpl.getStatic("msg-value");
            } else if (cmdTpl.isDynamic("msg-value")) {
                valueFunc = (l) -> cmdTpl.getDynamic("msg-value", l);
            } else {
                valueFunc = (l) -> null;
            }
        } else {
            throw new RuntimeException("\"msg-value\" field must be specified!");
        }

        return new PulsarProducerMapper(cmdTpl, pulsarSchema, producerFunc, keyFunc, valueFunc);
    }

    private LongFunction<PulsarOp> resolveConsumer(
        PulsarConsumerSpace clientSpace
    ) {
        LongFunction<String> topic_names_func;
        if (cmdTpl.isStatic("topic-names")) {
            topic_names_func = (l) -> cmdTpl.getStatic("topic-names");
        } else if (cmdTpl.isDynamic("topic-names")) {
            topic_names_func = (l) -> cmdTpl.getDynamic("topic-names", l);
        } else {
            topic_names_func = (l) -> null;
        }

        LongFunction<String> topics_pattern_func;
        if (cmdTpl.isStatic("topics-pattern")) {
            topics_pattern_func = (l) -> cmdTpl.getStatic("topics-pattern");
        } else if (cmdTpl.isDynamic("topics-pattern")) {
            topics_pattern_func = (l) -> cmdTpl.getDynamic("topics-pattern", l);
        } else {
            topics_pattern_func = (l) -> null;
        }

        LongFunction<String> subscription_name_func;
        if (cmdTpl.isStatic("subscription-name")) {
            subscription_name_func = (l) -> cmdTpl.getStatic("subscription-name");
        } else if (cmdTpl.isDynamic("subscription-name")) {
            subscription_name_func = (l) -> cmdTpl.getDynamic("subscription-name", l);
        } else {
            subscription_name_func = (l) -> null;
        }

        LongFunction<String> subscription_type_func;
        if (cmdTpl.isStatic("subscription-type")) {
            subscription_type_func = (l) -> cmdTpl.getStatic("subscription-type");
        } else if (cmdTpl.isDynamic("subscription-type")) {
            subscription_type_func = (l) -> cmdTpl.getDynamic("subscription-type", l);
        } else {
            subscription_type_func = (l) -> null;
        }

        LongFunction<String> consumer_name_func;
        if (cmdTpl.isStatic("consumer-name")) {
            consumer_name_func = (l) -> cmdTpl.getStatic("consumer-name");
        } else if (cmdTpl.isDynamic("consumer-name")) {
            consumer_name_func = (l) -> cmdTpl.getDynamic("consumer-name", l);
        } else {
            consumer_name_func = (l) -> null;
        }

        LongFunction<Consumer<?>> consumerFunc = (l) ->
            clientSpace.getConsumer(
                topic_names_func.apply(l),
                topics_pattern_func.apply(l),
                subscription_name_func.apply(l),
                subscription_type_func.apply(l),
                consumer_name_func.apply(l)
            );

        return new PulsarConsumerMapper(cmdTpl, pulsarSchema, consumerFunc);
    }

    private LongFunction<PulsarOp> resolveReader(
        PulsarReaderSpace clientSpace
    ) {
        LongFunction<String> topic_name_func;
        if (cmdTpl.isStatic("topic-name")) {
            topic_name_func = (l) -> cmdTpl.getStatic("topic-name");
        } else if (cmdTpl.isDynamic("topic-name")) {
            topic_name_func = (l) -> cmdTpl.getDynamic("topic-name", l);
        } else {
            topic_name_func = (l) -> null;
        }

        LongFunction<String> reader_name_func;
        if (cmdTpl.isStatic("reader-name")) {
            reader_name_func = (l) -> cmdTpl.getStatic("reader-name");
        } else if (cmdTpl.isDynamic("reader-name")) {
            reader_name_func = (l) -> cmdTpl.getDynamic("reader-name", l);
        } else {
            reader_name_func = (l) -> null;
        }

        LongFunction<String> start_msg_pos_str_func;
        if (cmdTpl.isStatic("start-msg-position")) {
            start_msg_pos_str_func = (l) -> cmdTpl.getStatic("start-msg-position");
        } else if (cmdTpl.isDynamic("start-msg-position")) {
            start_msg_pos_str_func = (l) -> cmdTpl.getDynamic("start-msg-position", l);
        } else {
            start_msg_pos_str_func = (l) -> null;
        }

        LongFunction<Reader<?>> readerFunc = (l) ->
            clientSpace.getReader(
                topic_name_func.apply(l),
                reader_name_func.apply(l),
                start_msg_pos_str_func.apply(l)
            );

        return new PulsarReaderMapper(cmdTpl, pulsarSchema, readerFunc);
    }

    @Override
    public PulsarOp apply(long value) {
        return opFunc.apply(value);
    }
}
