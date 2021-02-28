package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import org.apache.commons.lang3.StringUtils;
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
        if ( !StringUtils.isBlank(cycleTopicNames) ) {
            return cycleTopicNames;
        }

        String globalTopicNames = pulsarNBClientConf.getConsumerTopicNames();
        if ( !StringUtils.isBlank(globalTopicNames) ) {
            return globalTopicNames;
        }

        return "";
    }
    private List<String> getEffectiveTopicNames(String cycleTopicNames) {
        String effectiveTopicNamesStr = getEffectiveTopicNamesStr(cycleTopicNames);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if ( !StringUtils.isBlank(name) )
                effectiveTopicNameList.add(name.trim());
        }


        return effectiveTopicNameList;
    }

    private String getEffectiveTopicPatternStr(String cycleTopicsPattern) {
        if ( !StringUtils.isBlank(cycleTopicsPattern) ) {
            return cycleTopicsPattern;
        }

        String globalTopicsPattern = pulsarNBClientConf.getConsumerTopicPattern();
        if ( !StringUtils.isBlank(globalTopicsPattern) ) {
            return globalTopicsPattern;
        }

        return "";
    }
    private Pattern getEffectiveTopicPattern(String cycleTopicsPattern) {
        String effectiveTopicsPatternStr = getEffectiveTopicPatternStr(cycleTopicsPattern);
        Pattern topicsPattern;
        try {
            if ( !StringUtils.isBlank(effectiveTopicsPatternStr) )
                topicsPattern = Pattern.compile(effectiveTopicsPatternStr);
            else
                topicsPattern = null;
        }
        catch (PatternSyntaxException pse) {
            topicsPattern = null;
        }
        return topicsPattern;
    }

    private String getEffectiveSubscriptionName(String cycleSubscriptionName) {
        if ( !StringUtils.isBlank(cycleSubscriptionName) ) {
            return cycleSubscriptionName;
        }

        String globalSubscriptionName = pulsarNBClientConf.getConsumerSubscriptionName();
        if ( !StringUtils.isBlank(globalSubscriptionName) ) {
            return globalSubscriptionName;
        }

        return "default-subs";
    }

    private String getEffectiveSubscriptionTypeStr(String cycleSubscriptionType) {
        if ( !StringUtils.isBlank(cycleSubscriptionType) ) {
            return cycleSubscriptionType;
        }

        String globalSubscriptionType = pulsarNBClientConf.getConsumerSubscriptionType();
        if ( !StringUtils.isBlank(globalSubscriptionType) ) {
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
        if ( !StringUtils.isBlank(cycleConsumerName) ) {
            return cycleConsumerName;
        }

        String globalConsumerName = pulsarNBClientConf.getConsumerName();
        if ( !StringUtils.isBlank(globalConsumerName) ) {
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
        Pattern topicsPattern = getEffectiveTopicPattern(cycleTopicsPattern);
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        if ( topicNames.isEmpty() && (topicsPattern == null) ) {
            throw new RuntimeException("\"topicName\" and \"topicsPattern\" can't be empty/invalid at the same time!");
        }

        String encodedStr;
        if ( !topicNames.isEmpty() ) {
            encodedStr = PulsarActivityUtil.encode(
                consumerName,
                subscriptionName,
                StringUtils.join(topicNames, "|") );
        }
        else {
            encodedStr = PulsarActivityUtil.encode(
                consumerName,
                subscriptionName,
                topicsPatternStr );
        }
        Consumer<?> consumer = consumers.get(encodedStr);

        if (consumer == null) {
            PulsarClient pulsarClient = getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> consumerConf = pulsarNBClientConf.getConsumerConfMap();

            // Explicit topic names will take precedence over topics pattern
            if ( !topicNames.isEmpty() ) {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
                consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.toString(), topicNames);
            }
            else {
                consumerConf.remove(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
                consumerConf.put(
                    PulsarActivityUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label,
                    getEffectiveTopicPattern(cycleTopicsPattern));
            }

            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label, subscriptionName);
            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label, subscriptionType);
            consumerConf.put(PulsarActivityUtil.CONSUMER_CONF_STD_KEY.consumerName.label, consumerName);

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
