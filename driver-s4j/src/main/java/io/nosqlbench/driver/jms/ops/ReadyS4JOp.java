package io.nosqlbench.driver.jms.ops;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import io.nosqlbench.driver.jms.S4JSpaceCache;
import io.nosqlbench.driver.jms.excption.S4JDriverParamException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class ReadyS4JOp implements OpDispenser<S4JOp> {

    private final static Logger logger = LogManager.getLogger(ReadyS4JOp.class);

    private final OpTemplate optpl;
    private final CommandTemplate cmdTpl;
    private final S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;

    private final LongFunction<S4JOp> opFunc;


    public ReadyS4JOp(OpTemplate optpl, S4JSpaceCache s4JSpaceCache, S4JActivity s4JActivity) {
        this.optpl = optpl;
        this.cmdTpl = new CommandTemplate(this.optpl);
        this.s4JActivity = s4JActivity;

        String client_name = lookupStaticParameter("client", false, "default");
        this.s4JSpace = s4JSpaceCache.getAssociatedSpace(client_name);
        this.s4JSpace.setCmdTpl(cmdTpl);

        // Initialize JMS connection and sessions
        this.s4JSpace.initializeS4JConnectionFactory(s4JActivity.getS4JConnInfo());
        this.s4JActivity.setS4JActivityStartTimeMills(System.currentTimeMillis());

        this.s4JSpace.resetTotalOpResponseCnt();

        this.opFunc = resolveS4JOp();
    }


    private String lookupStaticParameter(String parameterName) {
        return lookupStaticParameter(parameterName, false, null);
    }
    private String lookupStaticParameter(String parameterName, boolean required) {
        return lookupStaticParameter(parameterName, required, null);
    }
    private String lookupStaticParameter(String parameterName, boolean required, String defaultValue) {
        if (cmdTpl.containsKey(parameterName)) {
            if (cmdTpl.isStatic(parameterName)) {
                return cmdTpl.getStatic(parameterName);
            } else if (cmdTpl.isDynamic(parameterName)) {
                throw new S4JDriverParamException("\"" + parameterName + "\" parameter must be static");
            } else {
                return defaultValue;
            }
        } else {
            if (required) {
                throw new S4JDriverParamException("\"" + parameterName + "\" field must be specified!");
            } else {
                return defaultValue;
            }
        }
    }

    private LongFunction<String> lookupParameterFunc(String parameterName) {
        return lookupParameterFunc(parameterName, false, null);
    }

    private LongFunction<String> lookupParameterFunc(String parameterName, boolean required) {
        return lookupParameterFunc(parameterName, required, null);
    }
    private LongFunction<String> lookupParameterFunc(String parameterName, boolean required, String defaultValue) {
        if (cmdTpl.containsKey(parameterName)) {
            LongFunction<String> lookupFunc;
            if (cmdTpl.isStatic(parameterName)) {
                String staticValue = cmdTpl.getStatic(parameterName);
                lookupFunc = (l) -> staticValue;
            } else if (cmdTpl.isDynamic(parameterName)) {
                lookupFunc = (l) -> cmdTpl.getDynamic(parameterName, l);
            } else {
                lookupFunc = (l) -> defaultValue;
            }
            return lookupFunc;
        } else {
            if (required) {
                throw new S4JDriverParamException("\"" + parameterName + "\" field must be specified!");
            } else {
                return (l) -> defaultValue;
            }
        }
    }


    public S4JOp apply(long value) { return opFunc.apply(value); }

    public LongFunction<S4JOp> resolveS4JOp() {

        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new RuntimeException("Statement parameter \"optype\" must be static and have a valid value!");
        }
        String stmtOpType = cmdTpl.getStatic("optype");

        // Doc-level parameter: temporary_dest (default: false) -- static
        String tempDestBoolStr = lookupStaticParameter(S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label, false, "false");
        boolean tempDestBool = BooleanUtils.toBoolean(tempDestBoolStr);
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label, tempDestBoolStr);

        // Doc-level parameter: dest_type (default: Topic) -- static
        // TODO - this can be extended to dynamic (dynamically choosing between 'Queue' and 'Topic')
        String destTypeStr = lookupStaticParameter(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label, false, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label);
        LongFunction<String> destTypeStrFunc = (l) -> destTypeStr;
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label, destTypeStr);

        // Doc-level parameter: dest_name - dynamic
        LongFunction<String> destNameStrFunc = lookupParameterFunc(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label, true, "persistent://public/default/nb4_s4j_test");
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label, destNameStrFunc.apply(0));

        // Doc-level parameter: async_api (default: true) - static
        String asyncAPIBoolStr = lookupStaticParameter(S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, false, "true");
        boolean asyncAPIBool = BooleanUtils.toBoolean(asyncAPIBoolStr);
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, asyncAPIBoolStr);

        // Doc-level parameter: txn_batch_num - static
        String txnBatchNumStr = lookupStaticParameter(S4JActivityUtil.DOC_LEVEL_PARAMS.TXN_BATCH_NUM.label, false, "0");
        int txnBatchNum = NumberUtils.toInt(txnBatchNumStr, 0);
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.TXN_BATCH_NUM.label, txnBatchNumStr);

        // Doc-level parameter: blocking_msg_recv (default: false) - static
        String blockingMsgRecvBoolStr = lookupStaticParameter(S4JActivityUtil.DOC_LEVEL_PARAMS.BLOCKING_MSG_RECV.label, false, "false");
        boolean blockingMsgRecvBool = BooleanUtils.toBoolean(blockingMsgRecvBoolStr);
        logger.info("{}: {}", S4JActivityUtil.DOC_LEVEL_PARAMS.BLOCKING_MSG_RECV.label, blockingMsgRecvBoolStr);

        if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ.label)) {
            return resolveMsgRead(
                false,
                false,
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED.label)) {
            return resolveMsgRead(
                false,
                true,
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_DURABLE.label)) {
            return resolveMsgRead(
                true,
                false,
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED_DURABLE.label)) {
            return resolveMsgRead(
                true,
                true,
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_BROWSE.label)) {
            return resolveMsgBrowse(
                tempDestBool,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBool,
                txnBatchNum,
                blockingMsgRecvBool);
        } else {
            throw new RuntimeException("Unsupported JMS operation type");
        }
    }

    private LongFunction<S4JOp> resolveMsgSend(
        boolean tempDestBool,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        boolean asyncAPIBool,
        int txnBatchNum,
        boolean blockingMsgRecvBool
    ) {
        // JMS message headers
        LongFunction<String> msgHeaderJsonStrFunc = lookupParameterFunc("msg_header");

        // JMS message properties
        LongFunction<String> msgPropJsonStrFunc = lookupParameterFunc("msg_property");

        // JMS message type
        LongFunction<String> msgTypeFunc = lookupParameterFunc("msg_type");

        // JMS message body
        LongFunction<String> msgBodyRawJsonStrFunc = lookupParameterFunc("msg_body");

        return new S4JMsgSendMapper(
            s4JSpace,
            s4JActivity,
            tempDestBool,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBool,
            txnBatchNum,
            blockingMsgRecvBool,
            msgHeaderJsonStrFunc,
            msgPropJsonStrFunc,
            msgTypeFunc,
            msgBodyRawJsonStrFunc);
    }

    private LongFunction<S4JOp> resolveMsgRead(
        boolean durableConsumer,                    // only relevant for Topic
        boolean sharedConsumer,                     // only relevant for Topic
        boolean tempDestBool,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        boolean asyncAPIBool,
        int txnBatchNum,
        boolean blockingMsgRecvBool
    ) {
        // subscription name - only relevant for Topic
        LongFunction<String> subNameStrFunc = lookupParameterFunc("subscription_name");

        // message acknowledgement ratio
        String msgAckRatioStr = lookupStaticParameter("msg_ack_ratio");
        float msgAckRatio = NumberUtils.toFloat(msgAckRatioStr);
        if (msgAckRatio < 0)
            msgAckRatio = 0.0f;
        else if (msgAckRatio > 1)
            msgAckRatio = 1.0f;

        // message selector - only relevant for Topic
        LongFunction<String> msgSelectorStrFunc = lookupParameterFunc("msg_selector");

        // non local
        String noLocalBoolStr = lookupStaticParameter("no_local");
        boolean noLocalBool = BooleanUtils.toBoolean(noLocalBoolStr);

        // read timeout
        LongFunction<Long> readTimeoutFunc = (l) -> 0L;
        if (cmdTpl.containsKey("read_timeout")) {
            if (cmdTpl.isStatic("read_timeout")) {
                readTimeoutFunc = (l) -> NumberUtils.toLong(cmdTpl.getStatic("read_timeout"));
            } else {
                readTimeoutFunc = (l) -> NumberUtils.toLong(cmdTpl.getDynamic("read_timeout", l));
            }
        }

        // receive no wait
        String recvNoWaitBoolStr = lookupStaticParameter("no_wait");
        boolean recvNoWaitBool = BooleanUtils.toBoolean(recvNoWaitBoolStr);

        return new S4JMsgReadMapper(
            s4JSpace,
            s4JActivity,
            durableConsumer,
            sharedConsumer,
            tempDestBool,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBool,
            txnBatchNum,
            blockingMsgRecvBool,
            subNameStrFunc,
            msgAckRatio,
            msgSelectorStrFunc,
            noLocalBool,
            readTimeoutFunc,
            recvNoWaitBool);
    }

    private LongFunction<S4JOp> resolveMsgBrowse(
        boolean tempDestBool,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        boolean asyncAPIBool,
        int txnBatchNum,
        boolean blockingMsgRecvBool
    ) {
        LongFunction<String> msgSelectorStrFunc =
            lookupParameterFunc("msg_selector", false, "");

        return new S4JMsgBrowseMapper(
            s4JSpace,
            s4JActivity,
            tempDestBool,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBool,
            txnBatchNum,
            blockingMsgRecvBool,
            msgSelectorStrFunc);
    }
}
