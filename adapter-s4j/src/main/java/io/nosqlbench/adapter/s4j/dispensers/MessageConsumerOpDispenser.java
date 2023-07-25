/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.s4j.dispensers;

import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.ops.MessageConsumerOp;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil;
import io.nosqlbench.adapter.s4j.util.S4JJMSContextWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class MessageConsumerOpDispenser extends S4JBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    // Doc-level parameter: blocking_msg_recv (default: false)
    protected final boolean blockingMsgRecv;
    // Doc-level parameter: shared_topic (default: false)
    // - only applicable to Topic as the destination type
    protected final boolean sharedTopic;
    // Doc-level parameter: durable_topic (default: false)
    // - only applicable to Topic as the destination type
    protected final boolean durableTopic;
    // default value: false
    private final boolean noLocal;
    // default value: 0
    // value <= 0 : no timeout
    private final int readTimeout;
    // default value: false
    private final boolean recvNoWait;
    // default value: 1.0 (all received messages are acknowledged)
    // value must be between 0 and 1 (inclusive)
    private final float msgAckRatio;
    // default value: 0
    // value <= 0 : no slow message ack
    private final int slowAckInSec;
    private final LongFunction<String> subNameStrFunc;
    private final LongFunction<String> localMsgSelectorFunc;

    // Generally the consumer related configurations can be set in the global "config.properties" file,
    //   which can be applied to many testing scenarios.
    // Setting them here will allow scenario-specific customer configurations. At the moment, only the
    //   DLT related settings are supported
    private final Map<String, Object> combinedS4jConfigObjMap = new HashMap<>();


    public MessageConsumerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      S4JSpace s4jSpace) {
        super(adapter, op, tgtNameFunc, s4jSpace);

        this.blockingMsgRecv =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.BLOCKING_MSG_RECV.label, Boolean.FALSE);
        this.sharedTopic =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.SHARED_TOPIC.label, Boolean.FALSE);
        this.durableTopic =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.DURABLE_TOPIC.label, Boolean.FALSE);
        this.noLocal =
            parsedOp.getStaticConfigOr("no_local", Boolean.FALSE);
        this.readTimeout =
            parsedOp.getStaticConfigOr("read_timeout", 0);
        this.recvNoWait =
            parsedOp.getStaticConfigOr("no_wait", Boolean.FALSE);
        this.msgAckRatio =
            parsedOp.getStaticConfigOr("msg_ack_ratio", 1.0f);
        this.slowAckInSec =
            parsedOp.getStaticConfigOr("slow_ack_in_sec", 0);
        this.localMsgSelectorFunc =
            lookupOptionalStrOpValueFunc("msg_selector");

        // Subscription name is OPTIONAL for queue and non-shared, non-durable topic;
        // but mandatory for shared or shared topic
        if ( StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.QUEUE.label) ||
             ( StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.TOPIC.label) &&
               !durableTopic && !sharedTopic) ) {
            this.subNameStrFunc =
                lookupOptionalStrOpValueFunc("subscription_name");
        }
        else {
            this.subNameStrFunc =
                lookupMandtoryStrOpValueFunc("subscription_name");
        }

        String[] stmtLvlConsumerConfKeyNameList = {
            "consumer.ackTimeoutMillis",
            "consumer.deadLetterPolicy",
            "consumer.negativeAckRedeliveryBackoff",
            "consumer.ackTimeoutRedeliveryBackoff"};
        HashMap<String, String> stmtLvlConsumerConfRawMap = new HashMap<>();
        for (String confKey : stmtLvlConsumerConfKeyNameList ) {
            String confVal = parsedOp.getStaticConfigOr(confKey, "");
            stmtLvlConsumerConfRawMap.put(
                StringUtils.substringAfter(confKey, "consumer."),
                confVal);
        }

        this.combinedS4jConfigObjMap.putAll(
            s4jSpace.getS4JClientConf().mergeExtraConsumerConfig(stmtLvlConsumerConfRawMap));
    }

    @Override
    public MessageConsumerOp apply(long cycle) {
        S4JJMSContextWrapper s4JJMSContextWrapper = getS4jJmsContextWrapper(cycle, this.combinedS4jConfigObjMap);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean commitTransact = super.commitTransaction(txnBatchNum, jmsContext.getSessionMode(), cycle);

        Destination destination;
        try {
            destination = getJmsDestination(
                s4JJMSContextWrapper, temporaryDest, destType, destNameStrFunc.apply(cycle));
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new RuntimeException("Unable to create the JMS destination!");
        }

        JMSConsumer jmsConsumer;
        try {
            jmsConsumer = getJmsConsumer(
                s4JJMSContextWrapper,
                destination,
                destType,
                subNameStrFunc.apply(cycle),
                localMsgSelectorFunc.apply(cycle),
                msgAckRatio,
                noLocal,
                durableTopic,
                sharedTopic,
                asyncAPI,
                slowAckInSec);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS consumer!");
        }

        return new MessageConsumerOp(
            s4jAdapterMetrics,
            s4jSpace,
            jmsContext,
            destination,
            asyncAPI,
            commitTransact,
            jmsConsumer,
            blockingMsgRecv,
            msgAckRatio,
            readTimeout,
            recvNoWait,
            slowAckInSec);
    }
}
