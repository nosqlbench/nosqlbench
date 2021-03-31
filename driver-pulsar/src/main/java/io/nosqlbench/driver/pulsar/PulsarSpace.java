package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Gauge;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.admin.Clusters;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * An instance of a pulsar client, along with all the cached objects which are normally
 * associated with it during a client session in a typical application.
 * A PulsarSpace is simply a named and cached set of objects which must be used together.
 */
public class PulsarSpace {

    private final static Logger logger = LogManager.getLogger(PulsarSpace.class);

    private final ConcurrentHashMap<String, Producer<?>> producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Reader<?>> readers = new ConcurrentHashMap<>();

    private final String spaceName;
    private final PulsarNBClientConf pulsarNBClientConf;
    private final String pulsarSvcUrl;
    private final String webSvcUrl;
    private final PulsarAdmin pulsarAdmin;

    private final Set<String> pulsarClusterMetadata = new HashSet<>();

    private PulsarClient pulsarClient = null;
    private Schema<?> pulsarSchema = null;
    private final ActivityDef activityDef;

    public PulsarSpace(String name,
                       PulsarNBClientConf pulsarClientConf,
                       String pulsarSvcUrl,
                       String webSvcUrl,
                       PulsarAdmin pulsarAdmin,
                       ActivityDef activityDef) {
        this.spaceName = name;
        this.pulsarNBClientConf = pulsarClientConf;
        this.pulsarSvcUrl = pulsarSvcUrl;
        this.webSvcUrl = webSvcUrl;
        this.pulsarAdmin = pulsarAdmin;
        this.activityDef = activityDef;

        createPulsarClientFromConf();
        createPulsarSchemaFromConf();

        try {
            Clusters clusters = pulsarAdmin.clusters();
            List<String> stringList = clusters.getClusters();
            CollectionUtils.addAll(pulsarClusterMetadata, stringList.listIterator());

        } catch (PulsarAdminException e) {
            String errMsg = "Fail to create PulsarClient from global configuration: " + e.getMessage();
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
    }

    private void createPulsarClientFromConf() {
        ClientBuilder clientBuilder = PulsarClient.builder();

        try {
            Map<String, Object> clientConf = pulsarNBClientConf.getClientConfMap();
            // Override "client.serviceUrl" setting in config.properties
            clientConf.remove("serviceUrl", pulsarSvcUrl);

            pulsarClient = clientBuilder
                .loadConf(clientConf)
                .serviceUrl(pulsarSvcUrl)
                .build();
        } catch (PulsarClientException pce) {
            String errMsg = "Fail to create PulsarClient from global configuration: " + pce.getMessage();
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
    }

    private void createPulsarSchemaFromConf() {
        Object value = pulsarNBClientConf.getSchemaConfValue("schema.type");
        String schemaType = (value != null) ? value.toString() : "";

        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType)) {
            value = pulsarNBClientConf.getSchemaConfValue("schema.definition");
            String schemaDefStr = (value != null) ? value.toString() : "";
            pulsarSchema = PulsarActivityUtil.getAvroSchema(schemaType, schemaDefStr);
        } else if (PulsarActivityUtil.isPrimitiveSchemaTypeStr(schemaType)) {
            pulsarSchema = PulsarActivityUtil.getPrimitiveTypeSchema((schemaType));
        } else {
            throw new RuntimeException("Unsupported schema type string: " + schemaType + "; " +
                "Only primitive type and Avro type are supported at the moment!");
        }
    }

    public PulsarClient getPulsarClient() { return pulsarClient; }

    public PulsarNBClientConf getPulsarClientConf() {
        return pulsarNBClientConf;
    }

    public Schema<?> getPulsarSchema() {
        return pulsarSchema;
    }

    public PulsarAdmin getPulsarAdmin() { return pulsarAdmin; }

    public String getPulsarSvcUrl() {
        return pulsarSvcUrl;
    }

    public String getWebSvcUrl() { return webSvcUrl; }

    public Set<String> getPulsarClusterMetadata() { return pulsarClusterMetadata; }

    //////////////////////////////////////
    // Producer Processing --> start
    //////////////////////////////////////
    // Topic name IS mandatory
    // - It must be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveProducerTopicName(String cycleTopicName) {
        if (!StringUtils.isBlank(cycleTopicName)) {
            return cycleTopicName;
        }

        String globalTopicName = pulsarNBClientConf.getProducerTopicName();
        if (!StringUtils.isBlank(globalTopicName)) {
            return globalTopicName;
        }

        throw new RuntimeException(" topic name must be set at either global level or cycle level!");
    }

    // Producer name is NOT mandatory
    // - It can be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveProducerName(String cycleProducerName) {
        if (!StringUtils.isBlank(cycleProducerName)) {
            return cycleProducerName;
        }

        String globalProducerName = pulsarNBClientConf.getProducerName();
        if (!StringUtils.isBlank(globalProducerName)) {
            return globalProducerName;
        }

        return "";
    }

    public Producer<?> getProducer(String cycleTopicName, String cycleProducerName) {
        String topicName = getEffectiveProducerTopicName(cycleTopicName);
        String producerName = getEffectiveProducerName(cycleProducerName);

        if (StringUtils.isBlank(topicName)) {
            throw new RuntimeException("Producer:: must specify a topic name either at the global level or the cycle level");
        }

        String encodedStr = PulsarActivityUtil.encode(producerName, topicName);
        Producer<?> producer = producers.computeIfAbsent(encodedStr, (pn -> {


            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> producerConf = pulsarNBClientConf.getProducerConfMap();
            producerConf.put(PulsarActivityUtil.PRODUCER_CONF_STD_KEY.topicName.label, topicName);
            String producerMetricsPrefix;
            if (!StringUtils.isBlank(producerName)) {
                producerConf.put(PulsarActivityUtil.PRODUCER_CONF_STD_KEY.producerName.label, producerName);
                producerMetricsPrefix = producerName + "_";
            } else {
                // we want a meaningful name for the producer
                // we are not appending the topic name
                producerMetricsPrefix = "producer" + producers.size() + "_" ;
            }

            producerMetricsPrefix += topicName + "_";

            producerMetricsPrefix = producerMetricsPrefix
                .replace("persistent://public/default/", "")  // default name for tests/demos (in all Pulsar examples) is persistent://public/default/test -> use just the topic name test
                .replace("non-persistent://", "") // always remove topic type
                .replace("persistent://", "")
                .replace("/","_"); // persistent://tenant/namespace/topicname -> tenant_namespace_topicname

            try {
                Producer<?> newProducer = pulsarClient.newProducer(pulsarSchema).loadConf(producerConf).create();
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "totalbytessent",safeExtractMetric(newProducer, (s -> s.getTotalBytesSent() + s.getNumBytesSent())));
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "totalmsgssent", safeExtractMetric(newProducer, (s -> s.getTotalMsgsSent() + s.getNumMsgsSent())));
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "totalsendfailed", safeExtractMetric(newProducer, (s -> s.getTotalSendFailed() + s.getNumSendFailed())));
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "totalacksreceived", safeExtractMetric(newProducer,(s -> s.getTotalAcksReceived() + s.getNumAcksReceived())));
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "sendbytesrate", safeExtractMetric(newProducer, ProducerStats::getSendBytesRate));
                ActivityMetrics.gauge(activityDef, producerMetricsPrefix + "sendmsgsrate", safeExtractMetric(newProducer, ProducerStats::getSendMsgsRate));
                return newProducer;
            } catch (PulsarClientException ple) {
                throw new RuntimeException("Unable to create a Pulsar producer!", ple);
            }

        }));

        return producer;
    }

    static Gauge<Object> safeExtractMetric(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
        return new GaugeImpl(producer, valueExtractor);
    }

    private static class GaugeImpl implements Gauge<Object> {
        private final Producer<?> producer;
        private final Function<ProducerStats, Object> valueExtractor;

        GaugeImpl(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
            this.producer = producer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // we need to synchronize on producer otherwise we could receive corrupted data
            synchronized(producer) {
                return valueExtractor.apply(producer.getStats());
            }
        }
    }

    //////////////////////////////////////
    // Producer Processing <-- end
    //////////////////////////////////////


    //////////////////////////////////////
    // Consumer Processing --> start
    //////////////////////////////////////
    private String getEffectiveTopicNamesStr(String cycleTopicNames) {
        if (!StringUtils.isBlank(cycleTopicNames)) {
            return cycleTopicNames;
        }

        String globalTopicNames = pulsarNBClientConf.getConsumerTopicNames();
        if (!StringUtils.isBlank(globalTopicNames)) {
            return globalTopicNames;
        }

        return "";
    }

    private List<String> getEffectiveTopicNames(String cycleTopicNames) {
        String effectiveTopicNamesStr = getEffectiveTopicNamesStr(cycleTopicNames);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if (!StringUtils.isBlank(name))
                effectiveTopicNameList.add(name.trim());
        }


        return effectiveTopicNameList;
    }

    private String getEffectiveTopicPatternStr(String cycleTopicsPattern) {
        if (!StringUtils.isBlank(cycleTopicsPattern)) {
            return cycleTopicsPattern;
        }

        String globalTopicsPattern = pulsarNBClientConf.getConsumerTopicPattern();
        if (!StringUtils.isBlank(globalTopicsPattern)) {
            return globalTopicsPattern;
        }

        return "";
    }

    private Pattern getEffectiveTopicPattern(String cycleTopicsPattern) {
        String effectiveTopicsPatternStr = getEffectiveTopicPatternStr(cycleTopicsPattern);
        Pattern topicsPattern;
        try {
            if (!StringUtils.isBlank(effectiveTopicsPatternStr))
                topicsPattern = Pattern.compile(effectiveTopicsPatternStr);
            else
                topicsPattern = null;
        } catch (PatternSyntaxException pse) {
            topicsPattern = null;
        }
        return topicsPattern;
    }

    private String getEffectiveSubscriptionName(String cycleSubscriptionName) {
        if (!StringUtils.isBlank(cycleSubscriptionName)) {
            return cycleSubscriptionName;
        }

        String globalSubscriptionName = pulsarNBClientConf.getConsumerSubscriptionName();
        if (!StringUtils.isBlank(globalSubscriptionName)) {
            return globalSubscriptionName;
        }

        throw new RuntimeException("Consumer::Subscription name must be set at either global level or cycle level!");
    }

    private String getEffectiveSubscriptionTypeStr(String cycleSubscriptionType) {
        if (!StringUtils.isBlank(cycleSubscriptionType)) {
            return cycleSubscriptionType;
        }

        String globalSubscriptionType = pulsarNBClientConf.getConsumerSubscriptionType();
        if (!StringUtils.isBlank(globalSubscriptionType)) {
            return globalSubscriptionType;
        }

        return "";
    }

    private SubscriptionType getEffectiveSubscriptionType(String cycleSubscriptionType) {
        String effectiveSubscriptionStr = getEffectiveSubscriptionTypeStr(cycleSubscriptionType);
        SubscriptionType subscriptionType = SubscriptionType.Exclusive;

        if (!StringUtils.isBlank(effectiveSubscriptionStr)) {
            if (!PulsarActivityUtil.isValidSubscriptionType(effectiveSubscriptionStr)) {
                throw new RuntimeException("Consumer::Invalid subscription type (\"" +
                    effectiveSubscriptionStr + "\"). \nValid subscription types: " + PulsarActivityUtil.getValidSubscriptionTypeList());
            } else {
                subscriptionType = SubscriptionType.valueOf(effectiveSubscriptionStr);
            }
        }

        return subscriptionType;
    }

    private String getEffectiveConsumerName(String cycleConsumerName) {
        if (!StringUtils.isBlank(cycleConsumerName)) {
            return cycleConsumerName;
        }

        String globalConsumerName = pulsarNBClientConf.getConsumerName();
        if (!StringUtils.isBlank(globalConsumerName)) {
            return globalConsumerName;
        }

        return "";
    }

    public Consumer<?> getConsumer(String cycleTopicUri,
                                   String cycleTopicNames,
                                   String cycleTopicsPattern,
                                   String cycleSubscriptionName,
                                   String cycleSubscriptionType,
                                   String cycleConsumerName) {

        List<String> topicNames = getEffectiveTopicNames(cycleTopicNames);
        String topicsPatternStr = getEffectiveTopicPatternStr(cycleTopicsPattern);
        Pattern topicsPattern = getEffectiveTopicPattern(cycleTopicsPattern);
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        if (StringUtils.isBlank(cycleTopicUri) && topicNames.isEmpty() && (topicsPattern == null)) {
            throw new RuntimeException("Consumer:: \"topic_uri\", \"topic_names\" and \"topics_pattern\" parameters can't be all empty/invalid!");
        }

        String encodedStr;
        // precedence sequence:
        //    topic_names (consumer statement param) >
        //      topics_pattern (consumer statement param) >
        //        topic_uri (document level param)
        if (!topicNames.isEmpty()) {
            encodedStr = PulsarActivityUtil.encode(
                consumerName,
                subscriptionName,
                StringUtils.join(topicNames, "|"));
        } else if (topicsPattern != null) {
            encodedStr = PulsarActivityUtil.encode(
                consumerName,
                subscriptionName,
                topicsPatternStr);
        } else {
            encodedStr = PulsarActivityUtil.encode(
                consumerName,
                subscriptionName,
                cycleTopicUri);
        }

        Consumer<?> consumer = consumers.get(encodedStr);

        if (consumer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> consumerConf = new HashMap<>(pulsarNBClientConf.getConsumerConfMap());
            consumerConf.remove("timeout");

            // Explicit topic names will take precedence over topics pattern
            if (!topicNames.isEmpty()) {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
                consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label, topicNames);
            } else if (topicsPattern != null) {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
                consumerConf.put(
                    PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label,
                    getEffectiveTopicPattern(cycleTopicsPattern));
            } else {
                topicNames.add(cycleTopicUri);
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
                consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label, topicNames);
            }

            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label, subscriptionName);
            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label, subscriptionType);
            if (!StringUtils.isBlank(consumerName)) {
                consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.consumerName.label, consumerName);
            }

            try {
                consumer = pulsarClient.newConsumer(pulsarSchema).loadConf(consumerConf).subscribe();
            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar consumer!");
            }

            consumers.put(encodedStr, consumer);
        }

        return consumer;
    }
    //////////////////////////////////////
    // Consumer Processing <-- end
    //////////////////////////////////////


    //////////////////////////////////////
    // Reader Processing --> Start
    //////////////////////////////////////
    private String getEffectiveReaderTopicName(String cycleReaderTopicName) {
        if (!StringUtils.isBlank(cycleReaderTopicName)) {
            return cycleReaderTopicName;
        }

        String globalReaderTopicName = pulsarNBClientConf.getReaderTopicName();
        if (!StringUtils.isBlank(globalReaderTopicName)) {
            return globalReaderTopicName;
        }

        throw new RuntimeException("Reader topic name must be set at either global level or cycle level!");
    }

    private String getEffectiveReaderName(String cycleReaderName) {
        if (!StringUtils.isBlank(cycleReaderName)) {
            return cycleReaderName;
        }

        String globalReaderName = pulsarNBClientConf.getConsumerName();
        if (!StringUtils.isBlank(globalReaderName)) {
            return globalReaderName;
        }

        return "";
    }

    private String getEffectiveStartMsgPosStr(String cycleStartMsgPosStr) {
        if (!StringUtils.isBlank(cycleStartMsgPosStr)) {
            return cycleStartMsgPosStr;
        }

        String globalStartMsgPosStr = pulsarNBClientConf.getStartMsgPosStr();
        if (!StringUtils.isBlank(globalStartMsgPosStr)) {
            return globalStartMsgPosStr;
        }

        return PulsarActivityUtil.READER_MSG_POSITION_TYPE.latest.label;
    }

    public Reader<?> getReader(String cycleTopicName,
                               String cycleReaderName,
                               String cycleStartMsgPos) {

        String topicName = getEffectiveReaderTopicName(cycleTopicName);
        if (StringUtils.isBlank(topicName)) {
            throw new RuntimeException("Reader:: must specify a topic name either at the global level or the cycle level");
        }

        String readerName = getEffectiveReaderName(cycleReaderName);

        String startMsgPosStr = getEffectiveStartMsgPosStr(cycleStartMsgPos);
        if (!PulsarActivityUtil.isValideReaderStartPosition(startMsgPosStr)) {
            throw new RuntimeException("Reader:: Invalid value for Reader start message position!");
        }

        String encodedStr = PulsarActivityUtil.encode(topicName, readerName, startMsgPosStr);
        Reader<?> reader = readers.get(encodedStr);

        if (reader == null) {
            PulsarClient pulsarClient = getPulsarClient();

            Map<String, Object> readerConf = pulsarNBClientConf.getReaderConfMap();
            readerConf.put(PulsarActivityUtil.READER_CONF_STD_KEY.topicName.toString(), topicName);

            if (!StringUtils.isBlank(readerName)) {
                readerConf.put(PulsarActivityUtil.READER_CONF_STD_KEY.readerName.toString(), readerName);
            }

            // "reader.startMessagePos" is NOT a standard Pulsar reader conf
            readerConf.remove(PulsarActivityUtil.READER_CONF_CUSTOM_KEY.startMessagePos.label);

            try {
                ReaderBuilder<?> readerBuilder = pulsarClient.newReader(pulsarSchema).loadConf(readerConf);

                MessageId startMsgId = MessageId.latest;
                if (startMsgPosStr.equalsIgnoreCase(PulsarActivityUtil.READER_MSG_POSITION_TYPE.earliest.label)) {
                    startMsgId = MessageId.earliest;
                }
                //TODO: custom start message position is NOT supported yet
                //else if (startMsgPosStr.startsWith(PulsarActivityUtil.READER_MSG_POSITION_TYPE.custom.label)) {
                //    startMsgId = MessageId.latest;
                //}

                if (startMsgId != null) {
                    readerBuilder = readerBuilder.startMessageId(startMsgId);
                }

                reader = readerBuilder.create();
            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar reader!");
            }

            readers.put(encodedStr, reader);
        }

        return reader;
    }
    //////////////////////////////////////
    // Reader Processing <-- end
    //////////////////////////////////////
}
