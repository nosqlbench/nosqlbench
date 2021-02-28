package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PulsarConsumerSpace extends PulsarSpace {

    private final ConcurrentHashMap<String, Consumer<?>> consumers = new ConcurrentHashMap<>();

    public PulsarConsumerSpace(String name, PulsarNBClientConf pulsarClientConf) { super(name, pulsarClientConf); }

    private String getEffectiveTopicNamesStr(String cycleTopicNames) {
        if ((cycleTopicNames != null) && (!cycleTopicNames.isEmpty())) {
            return cycleTopicNames;
        }

        String globalTopicNames = pulsarNBClientConf.getConsumerTopicNames();
        if ((globalTopicNames != null) && (!globalTopicNames.isEmpty())) {
            return globalTopicNames;
        }

        return "";
    }
    private List<String> getEffectiveTopicNames(String cycleTopicNames) {
        String effectiveTopicNamesStr = getEffectiveTopicNamesStr(cycleTopicNames);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if ( !name.isEmpty() )
                effectiveTopicNameList.add(name.trim());
        }


        return effectiveTopicNameList;
    }

    private String getEffectiveTopicPatternStr(String cycleTopicsPattern) {
        if ((cycleTopicsPattern != null) && (!cycleTopicsPattern.isEmpty())) {
            return cycleTopicsPattern;
        }

        String globalTopicsPattern = pulsarNBClientConf.getConsumerTopicPattern();
        if ((globalTopicsPattern != null) && (!globalTopicsPattern.isEmpty())) {
            return globalTopicsPattern;
        }

        return "";
    }
    private Pattern getEffectiveTopicPattern(String cycleTopicsPattern) {
        String effecitveTopicsPatternStr = getEffectiveTopicPatternStr(cycleTopicsPattern);
        Pattern topicsPattern;
        try {
            topicsPattern = Pattern.compile(effecitveTopicsPatternStr);
        }
        catch (PatternSyntaxException pse) {
            topicsPattern = null;
        }
        return topicsPattern;
    }

    private String getEffectiveSubscriptionName(String cycleSubscriptionName) {
        if ((cycleSubscriptionName != null) && (!cycleSubscriptionName.isEmpty())) {
            return cycleSubscriptionName;
        }

        String globalSubscriptionName = pulsarNBClientConf.getConsumerSubscriptionName();
        if ((globalSubscriptionName != null) && (!globalSubscriptionName.isEmpty())) {
            return globalSubscriptionName;
        }

        return "default-subs";
    }

    private String getEffectiveSubscriptionTypeStr(String cycleSubscriptionType) {
        if ((cycleSubscriptionType != null) && (!cycleSubscriptionType.isEmpty())) {
            return cycleSubscriptionType;
        }

        String globalSubscriptionType = pulsarNBClientConf.getConsumerSubscriptionType();
        if ((globalSubscriptionType != null) && (!globalSubscriptionType.isEmpty())) {
            return globalSubscriptionType;
        }

        return "";
    }
    private SubscriptionType getEffectiveSubscriptionType(String cycleSubscriptionType) {
        String effectiveSubscriptionStr = getEffectiveSubscriptionTypeStr(cycleSubscriptionType);
        SubscriptionType subscriptionType;

        try {
            subscriptionType = SubscriptionType.valueOf(effectiveSubscriptionStr);
        }
        catch (IllegalArgumentException iae) {
            subscriptionType = SubscriptionType.Exclusive;
        }

        return subscriptionType;
    }

    private String getEffectiveConsumerName(String cycleConsumerName) {
        if ((cycleConsumerName != null) && (!cycleConsumerName.isEmpty())) {
            return cycleConsumerName;
        }

        String globalConsumerName = pulsarNBClientConf.getConsumerName();
        if ((globalConsumerName != null) && (!globalConsumerName.isEmpty())) {
            return globalConsumerName;
        }

        return "default-cons";
    }

    public Consumer<?> getConsumer(String cycleTopicNames,
                                   String cycleTopicsPattern,
                                   String cycleSubscriptionName,
                                   String cycleSubscriptionType,
                                   String cycleConsumerName) {

        String topicNamesStr = getEffectiveTopicNamesStr(cycleTopicNames);
        List<String> topicNames = getEffectiveTopicNames(cycleTopicNames);
        String topicsPatternStr = getEffectiveTopicPatternStr(cycleTopicsPattern);
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        String encodedStr = PulsarActivityUtil.encode(
            consumerName, subscriptionName, topicNamesStr, topicsPatternStr);
        Consumer<?> consumer = consumers.get(encodedStr);

        if (consumer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> consumerConf = pulsarNBClientConf.getConsumerConfMap();

            // Explicit topic names will take precedence over topics pattern
            if ( !topicNames.isEmpty() ) {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_KEY.topicsPattern.toString());
                consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_KEY.topicNames.toString(), topicNames);
            }
            else {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_KEY.topicNames.toString());
                if ( !topicsPatternStr.isEmpty() )
                    consumerConf.put(
                        PulsarActivityUtil.CONSUMER_CONF_KEY.topicsPattern.toString(),
                        getEffectiveTopicPattern(cycleTopicsPattern));
                else {
                    throw new RuntimeException("\"topicName\" and \"topicsPattern\" can't be empty/invalid at the same time!");
                }
            }

            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_KEY.subscriptionName.toString(), subscriptionName);
            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_KEY.subscriptionType.toString(), subscriptionType);
            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_KEY.consumerName.toString(), consumerName);

            try {
                consumer = pulsarClient.newConsumer(pulsarSchema).loadConf(consumerConf).subscribe();
            }
            catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar consumer!");
            }

            consumers.put(encodedStr, consumer);
        }

        return consumer;
    }
}
