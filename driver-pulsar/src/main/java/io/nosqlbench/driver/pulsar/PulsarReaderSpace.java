package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PulsarReaderSpace extends PulsarSpace {

    private final ConcurrentHashMap<String, Reader<?>> readers = new ConcurrentHashMap<>();

    public PulsarReaderSpace(String name, PulsarNBClientConf pulsarClientConf) {
        super(name, pulsarClientConf);
    }

    private String getEffectiveReaderTopicName(String cycleReaderTopicName) {
        if ( !StringUtils.isBlank(cycleReaderTopicName) ) {
            return cycleReaderTopicName;
        }

        String globalReaderTopicName = pulsarNBClientConf.getReaderTopicName();
        if ( !StringUtils.isBlank(globalReaderTopicName) ) {
            return globalReaderTopicName;
        }

        return "";
    }

    private String getEffectiveReaderName(String cycleReaderName) {
        if ( !StringUtils.isBlank(cycleReaderName) ) {
            return cycleReaderName;
        }

        String globalReaderName = pulsarNBClientConf.getConsumerName();
        if ( !StringUtils.isBlank(globalReaderName) ) {
            return globalReaderName;
        }

        return "default-read";
    }

    private String getEffectiveStartMsgPosStr(String cycleStartMsgPosStr) {
        if ( !StringUtils.isBlank(cycleStartMsgPosStr) ) {
            return cycleStartMsgPosStr;
        }

        String globalStartMsgPosStr = pulsarNBClientConf.getStartMsgPosStr();
        if ( !StringUtils.isBlank(globalStartMsgPosStr) ) {
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

        if ( StringUtils.isBlank(topicName) ) {
            throw new RuntimeException("Must specify a \"topicName\" for a reader!");
        }

        String encodedStr = PulsarActivityUtil.encode(cycleTopicName, cycleReaderName, cycleStartMsgPos);
        Reader<?> reader = readers.get(encodedStr);

        if (reader == null) {
            PulsarClient pulsarClient = getPulsarClient();

            Map<String, Object> readerConf = pulsarNBClientConf.getReaderConfMap();
            readerConf.put(PulsarActivityUtil.READER_CONF_STD_KEY.topicName.toString(), topicName);
            readerConf.put(PulsarActivityUtil.READER_CONF_STD_KEY.readerName.toString(), readerName);
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
            }
            catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar reader!");
            }

            readers.put(encodedStr, reader);
        }

        return reader;
    }
}
