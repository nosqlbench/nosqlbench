package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
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
import org.apache.pulsar.client.api.transaction.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * An instance of a pulsar client, along with all the cached objects which are normally
 * associated with it during a client session in a typical application.
 * A PulsarSpace is simply a named and cached set of objects which must be used together.
 */
public class PulsarSpace {

    private final static Logger logger = LogManager.getLogger(PulsarSpace.class);

    private final String spaceName;

    private final ConcurrentHashMap<String, Producer<?>> producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Reader<?>> readers = new ConcurrentHashMap<>();

    private final PulsarActivity pulsarActivity;
    private final ActivityDef activityDef;

    private final PulsarNBClientConf pulsarNBClientConf;
    private final String pulsarSvcUrl;
    private final String webSvcUrl;
    private final PulsarAdmin pulsarAdmin;
    private final PulsarClient pulsarClient;
    private final Schema<?> pulsarSchema;
    private final Set<String> pulsarClusterMetadata = new HashSet<>();
    private final Timer createTransactionTimer;

    public PulsarSpace(String name, PulsarActivity pulsarActivity) {
        this.spaceName = name;
        this.pulsarActivity = pulsarActivity;

        this.pulsarNBClientConf = pulsarActivity.getPulsarConf();
        this.pulsarSvcUrl = pulsarActivity.getPulsarSvcUrl();
        this.webSvcUrl = pulsarActivity.getWebSvcUrl();
        this.pulsarAdmin = pulsarActivity.getPulsarAdmin();
        this.pulsarClient = pulsarActivity.getPulsarClient();
        this.pulsarSchema = pulsarActivity.getPulsarSchema();
        this.activityDef = pulsarActivity.getActivityDef();
        this.createTransactionTimer = pulsarActivity.getCreateTransactionTimer();

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

    public PulsarNBClientConf getPulsarClientConf() { return pulsarNBClientConf; }
    public PulsarAdmin getPulsarAdmin() { return pulsarAdmin; }
    public PulsarClient getPulsarClient() { return pulsarClient; }
    public Schema<?> getPulsarSchema() { return pulsarSchema; }
    public String getPulsarSvcUrl() { return pulsarSvcUrl;}
    public String getWebSvcUrl() { return webSvcUrl; }
    public Set<String> getPulsarClusterMetadata() { return pulsarClusterMetadata; }


    // Properly shut down all Pulsar objects (producers, consumers, etc.) that are associated with this space
    public void shutdownPulsarSpace() {
        try {
            for (Producer<?> producer : producers.values()) {
                if (producer != null) producer.close();
            }

            for (Consumer<?> consumer : consumers.values()) {
                if (consumer != null) consumer.close();
            }

            for (Reader<?> reader : readers.values()) {
                if (reader != null) reader.close();
            }

            if (pulsarAdmin != null) pulsarAdmin.close();

            if (pulsarClient != null) pulsarClient.close();
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected error when closing Pulsar objects!");
        }
    }

    /**
     * Get a proper Pulsar API metrics prefix depending on the API type
     *
     * @param apiType - Pulsar API type: producer, consumer, reader, etc.
     * @param apiObjName - actual name of a producer, a consumer, a reader, etc.
     * @param topicName - topic name
     * @return String
     */
    private String getPulsarAPIMetricsPrefix(String apiType, String apiObjName, String topicName) {
        String apiMetricsPrefix;

        if (!PulsarActivityUtil.isValidPulsarApiType(apiType)) {
            throw new RuntimeException(
                "Incorrect Pulsar API type. Valid type list: " + PulsarActivityUtil.getValidPulsarApiTypeList());
        }

        if (!StringUtils.isBlank(apiObjName)) {
            apiMetricsPrefix = apiObjName + "_";
        }
        else {
            // we want a meaningful name for the API object (producer, consumer, reader, etc.)
            // we are not appending the topic name
            apiMetricsPrefix = apiType;

            if (apiType.equalsIgnoreCase(PulsarActivityUtil.PULSAR_API_TYPE.PRODUCER.label))
                apiMetricsPrefix += producers.size();
            else if (apiType.equalsIgnoreCase(PulsarActivityUtil.PULSAR_API_TYPE.CONSUMER.label))
                apiMetricsPrefix += consumers.size();
            else if (apiType.equalsIgnoreCase(PulsarActivityUtil.PULSAR_API_TYPE.READER.label))
                apiMetricsPrefix += readers.size();

            apiMetricsPrefix += "_";
        }

        apiMetricsPrefix += topicName + "_";
        apiMetricsPrefix = apiMetricsPrefix
            // default name for tests/demos (in all Pulsar examples) is persistent://public/default/test -> use just the topic name test
            .replace("persistent://public/default/", "")
            // always remove topic type
            .replace("non-persistent://", "")
            .replace("persistent://", "")
            // persistent://tenant/namespace/topicname -> tenant_namespace_topicname
            .replace("/","_");

        return apiMetricsPrefix;
    }


    //////////////////////////////////////
    // Producer Processing --> start
    //////////////////////////////////////
    //
    private static class ProducerGaugeImpl implements Gauge<Object> {
        private final Producer<?> producer;
        private final Function<ProducerStats, Object> valueExtractor;

        ProducerGaugeImpl(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
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
    static Gauge<Object> producerSafeExtractMetric(Producer<?> producer, Function<ProducerStats, Object> valueExtractor) {
        return new ProducerGaugeImpl(producer, valueExtractor);
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

    public Supplier<Transaction> getTransactionSupplier() {
        PulsarClient pulsarClient = getPulsarClient();
        return () -> {
            try (Timer.Context time = createTransactionTimer.time() ){
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

        throw new RuntimeException("Producer topic name must be set at either global level or cycle level!");
    }

    public Producer<?> getProducer(String cycleTopicName, String cycleProducerName) {
        String topicName = getEffectiveProducerTopicName(cycleTopicName);
        String producerName = getEffectiveProducerName(cycleProducerName);

        if (StringUtils.isBlank(topicName)) {
            throw new RuntimeException("Producer:: must specify a topic name");
        }

        String producerCacheKey = PulsarActivityUtil.buildCacheKey(producerName, topicName);
        Producer<?> producer = producers.get(producerCacheKey);

        if (producer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> producerConf = pulsarNBClientConf.getProducerConfMap();

            // Remove global level settings: "topicName" and "producerName"
            producerConf.remove(PulsarActivityUtil.PRODUCER_CONF_STD_KEY.topicName.label);
            producerConf.remove(PulsarActivityUtil.PRODUCER_CONF_STD_KEY.producerName.label);

            String producerMetricsPrefix = getPulsarAPIMetricsPrefix(
                PulsarActivityUtil.PULSAR_API_TYPE.PRODUCER.label,
                producerName,
                topicName);

            try {
                ProducerBuilder<?> producerBuilder = pulsarClient.
                    newProducer(pulsarSchema).
                    loadConf(producerConf).
                    topic(topicName);

                if (!StringUtils.isAnyBlank(producerName)) {
                    producerBuilder = producerBuilder.producerName(producerName);
                }

                producer = producerBuilder.create();
                producers.put(producerCacheKey, producer);

                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "total_bytes_sent",
                    producerSafeExtractMetric(producer, (s -> s.getTotalBytesSent() + s.getNumBytesSent())));
                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "total_msg_sent",
                    producerSafeExtractMetric(producer, (s -> s.getTotalMsgsSent() + s.getNumMsgsSent())));
                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "total_send_failed",
                    producerSafeExtractMetric(producer, (s -> s.getTotalSendFailed() + s.getNumSendFailed())));
                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "total_ack_received",
                    producerSafeExtractMetric(producer,(s -> s.getTotalAcksReceived() + s.getNumAcksReceived())));
                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "send_bytes_rate",
                    producerSafeExtractMetric(producer, ProducerStats::getSendBytesRate));
                ActivityMetrics.gauge(activityDef,
                    producerMetricsPrefix + "send_msg_rate",
                    producerSafeExtractMetric(producer, ProducerStats::getSendMsgsRate));
            }
            catch (PulsarClientException ple) {
                throw new RuntimeException("Unable to create a Pulsar producer!", ple);
            }
        }

        return producer;
    }
    //
    //////////////////////////////////////
    // Producer Processing <-- end
    //////////////////////////////////////


    //////////////////////////////////////
    // Consumer Processing --> start
    //////////////////////////////////////
    //
    private static class ConsumerGaugeImpl implements Gauge<Object> {
        private final Consumer<?> consumer;
        private final Function<ConsumerStats, Object> valueExtractor;

        ConsumerGaugeImpl(Consumer<?> consumer, Function<ConsumerStats, Object> valueExtractor) {
            this.consumer = consumer;
            this.valueExtractor = valueExtractor;
        }

        @Override
        public Object getValue() {
            // see Pulsar bug https://github.com/apache/pulsar/issues/10100
            // - this is a bug report for producer stats.
            // - assume this also applies to consumer stats.
            synchronized(consumer) {
                return valueExtractor.apply(consumer.getStats());
            }
        }
    }
    static Gauge<Object> consumerSafeExtractMetric(Consumer<?> consumer, Function<ConsumerStats, Object> valueExtractor) {
        return new ConsumerGaugeImpl(consumer, valueExtractor);
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

    public Consumer<?> getConsumer(String cycleTopicName,
                                   String cycleSubscriptionName,
                                   String cycleSubscriptionType,
                                   String cycleConsumerName) {
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        if ( subscriptionType.equals(SubscriptionType.Exclusive) && (activityDef.getThreads() > 1) ) {
            throw new RuntimeException("Consumer:: trying to create multiple consumers of " +
                "\"Exclusive\" subscription type under the same subscription name to the same topic!");
        }

        if (StringUtils.isAnyBlank(cycleTopicName, subscriptionName)) {
            throw new RuntimeException("Consumer:: must specify a topic name and a subscription name");
        }

        String consumerCacheKey = PulsarActivityUtil.buildCacheKey(consumerName, subscriptionName, cycleTopicName);
        Consumer<?> consumer = consumers.get(consumerCacheKey);

        if (consumer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible consumer settings that are set at global level
            Map<String, Object> consumerConf = new HashMap<>(pulsarNBClientConf.getConsumerConfMap());

            // Remove global level settings:
            // - "topicNames", "topicsPattern", "subscriptionName", "subscriptionType", "consumerName"
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.consumerName.label);
            // Remove non-standard consumer configuration properties
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_CUSTOM_KEY.timeout.label);

            try {
                ConsumerBuilder<?> consumerBuilder = pulsarClient.
                    newConsumer(pulsarSchema).
                    loadConf(consumerConf).
                    topic(cycleTopicName).
                    subscriptionName(subscriptionName).
                    subscriptionType(subscriptionType);

                if (!StringUtils.isBlank(consumerName)) {
                    consumerBuilder = consumerBuilder.consumerName(consumerName);
                }

                consumer = consumerBuilder.subscribe();

                String consumerMetricsPrefix = getPulsarAPIMetricsPrefix(
                    PulsarActivityUtil.PULSAR_API_TYPE.CONSUMER.label,
                    consumerName,
                    cycleTopicName);

                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "total_bytes_recv",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalBytesReceived() + s.getNumBytesReceived())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "total_msg_recv",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalMsgsReceived() + s.getNumMsgsReceived())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "total_recv_failed",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalReceivedFailed() + s.getNumReceiveFailed())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "total_acks_sent",
                    consumerSafeExtractMetric(consumer,(s -> s.getTotalAcksSent() + s.getNumAcksSent())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "recv_bytes_rate",
                    consumerSafeExtractMetric(consumer, ConsumerStats::getRateBytesReceived));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "recv_msg_rate",
                    consumerSafeExtractMetric(consumer, ConsumerStats::getRateMsgsReceived));
            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar consumer!");
            }

            consumers.put(consumerCacheKey, consumer);
        }

        return consumer;
    }
    //
    //////////////////////////////////////
    // Consumer Processing <-- end
    //////////////////////////////////////


    //////////////////////////////////////
    // Multi-topic Consumer Processing --> start
    //////////////////////////////////////
    //
    private String getEffectiveConsumerTopicNameListStr(String cycleTopicNames) {
        if (!StringUtils.isBlank(cycleTopicNames)) {
            return cycleTopicNames;
        }

        String globalTopicNames = pulsarNBClientConf.getConsumerTopicNames();
        if (!StringUtils.isBlank(globalTopicNames)) {
            return globalTopicNames;
        }

        return "";
    }

    private List<String> getEffectiveConsumerTopicNameList(String cycleTopicNames) {
        String effectiveTopicNamesStr = getEffectiveConsumerTopicNameListStr(cycleTopicNames);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if (!StringUtils.isBlank(name))
                effectiveTopicNameList.add(name.trim());
        }

        return effectiveTopicNameList;
    }

    private String getEffectiveConsumerTopicPatternStr(String cycleTopicsPattern) {
        if (!StringUtils.isBlank(cycleTopicsPattern)) {
            return cycleTopicsPattern;
        }

        String globalTopicsPattern = pulsarNBClientConf.getConsumerTopicPattern();
        if (!StringUtils.isBlank(globalTopicsPattern)) {
            return globalTopicsPattern;
        }

        return "";
    }

    private Pattern getEffectiveConsumerTopicPattern(String cycleTopicsPattern) {
        String effectiveTopicsPatternStr = getEffectiveConsumerTopicPatternStr(cycleTopicsPattern);
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

    public Consumer<?> getMultiTopicConsumer(
        String cycleTopicUri,
        String cycleTopicNameList,
        String cycleTopicsPattern,
        String cycleSubscriptionName,
        String cycleSubscriptionType,
        String cycleConsumerName) {

        List<String> topicNameList = getEffectiveConsumerTopicNameList(cycleTopicNameList);
        String topicsPatternStr = getEffectiveConsumerTopicPatternStr(cycleTopicsPattern);
        Pattern topicsPattern = getEffectiveConsumerTopicPattern(cycleTopicsPattern);
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        if ( subscriptionType.equals(SubscriptionType.Exclusive) && (activityDef.getThreads() > 1) ) {
            throw new RuntimeException("Consumer:: trying to create multiple consumers of " +
                "\"Exclusive\" subscription type under the same subscription name to the same topic!");
        }

        if (StringUtils.isBlank(cycleTopicUri) && topicNameList.isEmpty() && (topicsPattern == null)) {
            throw new RuntimeException("Consumer:: \"topic_uri\", \"topic_names\" and \"topics_pattern\" parameters can't be all empty/invalid!");
        }

        // precedence sequence:
        //    topic_names (consumer statement param) >
        //      topics_pattern (consumer statement param) >
        //        topic_uri (document level param)
        String consumerTopicListString;
        if (!topicNameList.isEmpty()) {
            consumerTopicListString = String.join("|", topicNameList);
        } else if (topicsPattern != null) {
            consumerTopicListString = topicsPatternStr;
        } else {
            consumerTopicListString = cycleTopicUri;
        }
        String consumerCacheKey = PulsarActivityUtil.buildCacheKey(
            consumerName,
            subscriptionName,
            consumerTopicListString);

        Consumer<?> consumer = consumers.get(consumerCacheKey);

        if (consumer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> consumerConf = new HashMap<>(pulsarNBClientConf.getConsumerConfMap());

            // Remove global level settings:
            // - "topicNameList", "topicsPattern", "subscriptionName", "subscriptionType", "consumerName"
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.consumerName.label);
            // Remove non-standard consumer configuration properties
            consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_CUSTOM_KEY.timeout.label);

            try {
                ConsumerBuilder<?> consumerBuilder = pulsarClient.newConsumer(pulsarSchema).
                    loadConf(consumerConf).
                    subscriptionName(subscriptionName).
                    subscriptionType(subscriptionType).
                    consumerName(consumerName);

                if (!topicNameList.isEmpty()) {
                    consumerBuilder = consumerBuilder.topics(topicNameList);
                } else if (topicsPattern != null) {
                    consumerBuilder = consumerBuilder.topicsPattern(topicsPattern);
                } else {
                    consumerBuilder = consumerBuilder.topic(cycleTopicUri);
                }

                consumer = consumerBuilder.subscribe();

                String consumerMetricsPrefix = getPulsarAPIMetricsPrefix(
                    PulsarActivityUtil.PULSAR_API_TYPE.PRODUCER.label,
                    consumerName,
                    consumerTopicListString);

                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "totalBytesRecvd",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalBytesReceived() + s.getNumBytesReceived())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "totalMsgsRecvd",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalMsgsReceived() + s.getNumMsgsReceived())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "totalRecvdFailed",
                    consumerSafeExtractMetric(consumer, (s -> s.getTotalReceivedFailed() + s.getNumReceiveFailed())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "totalAcksSent",
                    consumerSafeExtractMetric(consumer,(s -> s.getTotalAcksSent() + s.getNumAcksSent())));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "recvdBytesRate",
                    consumerSafeExtractMetric(consumer, ConsumerStats::getRateBytesReceived));
                ActivityMetrics.gauge(activityDef,
                    consumerMetricsPrefix + "recvdMsgsRate",
                    consumerSafeExtractMetric(consumer, ConsumerStats::getRateMsgsReceived));

            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar consumer!");
            }

            consumers.put(consumerCacheKey, consumer);
        }

        return consumer;
    }
    //
    //////////////////////////////////////
    // Multi-topic Consumer Processing <-- end
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

        throw new RuntimeException("Reader:: Reader topic name must be set at either global level or cycle level!");
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
        String readerName = getEffectiveReaderName(cycleReaderName);
        String startMsgPosStr = getEffectiveStartMsgPosStr(cycleStartMsgPos);
        if (!PulsarActivityUtil.isValideReaderStartPosition(startMsgPosStr)) {
            throw new RuntimeException("Reader:: Invalid value for reader start message position!");
        }

        String readerCacheKey = PulsarActivityUtil.buildCacheKey(topicName, readerName, startMsgPosStr);
        Reader<?> reader = readers.get(readerCacheKey);

        if (reader == null) {
            PulsarClient pulsarClient = getPulsarClient();

            Map<String, Object> readerConf = pulsarNBClientConf.getReaderConfMap();

            // Remove global level settings: "topicName" and "readerName"
            readerConf.remove(PulsarActivityUtil.READER_CONF_STD_KEY.topicName.label);
            readerConf.remove(PulsarActivityUtil.READER_CONF_STD_KEY.readerName.label);
            // Remove non-standard reader configuration properties
            readerConf.remove(PulsarActivityUtil.READER_CONF_CUSTOM_KEY.startMessagePos.label);

            try {
                ReaderBuilder<?> readerBuilder = pulsarClient.
                    newReader(pulsarSchema).
                    loadConf(readerConf).
                    topic(topicName).
                    readerName(readerName);

                MessageId startMsgId = MessageId.latest;
                if (startMsgPosStr.equalsIgnoreCase(PulsarActivityUtil.READER_MSG_POSITION_TYPE.earliest.label)) {
                    startMsgId = MessageId.earliest;
                }
                //TODO: custom start message position is NOT supported yet
                //else if (startMsgPosStr.startsWith(PulsarActivityUtil.READER_MSG_POSITION_TYPE.custom.label)) {
                //    startMsgId = MessageId.latest;
                //}

                reader = readerBuilder.startMessageId(startMsgId).create();

            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar reader!");
            }

            readers.put(readerCacheKey, reader);
        }

        return reader;
    }
    //////////////////////////////////////
    // Reader Processing <-- end
    //////////////////////////////////////
}
