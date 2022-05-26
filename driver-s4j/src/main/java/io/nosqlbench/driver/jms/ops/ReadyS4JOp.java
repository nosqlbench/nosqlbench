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
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSContext;
import java.util.function.LongFunction;

public class ReadyS4JOp implements OpDispenser<S4JOp> {

    private final OpTemplate optpl;
    private final CommandTemplate cmdTpl;
    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;

    private final LongFunction<S4JOp> opFunc;

    public ReadyS4JOp(OpTemplate opTemplate, S4JActivity s4JActivity) {
        this.optpl = opTemplate;
        this.cmdTpl = new CommandTemplate(optpl);
        this.s4JActivity = s4JActivity;
        this.jmsContext = s4JActivity.getJmsContext();

        this.opFunc = resolveS4JOp();
    }

    public S4JOp apply(long value) { return opFunc.apply(value); }

    public LongFunction<S4JOp> resolveS4JOp() {

        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new RuntimeException("Statement parameter \"optype\" must be static and have a valid value!");
        }
        String stmtOpType = cmdTpl.getStatic("optype");

        // Doc-level parameter: temporary_dest (default: false)
        LongFunction<Boolean> tempDestBoolFunc = (l) -> false;
        if (cmdTpl.containsKey(S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label)) {
            if (cmdTpl.isStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label)) {
                tempDestBoolFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label));
            } else {
                throw new RuntimeException("\"" + S4JActivityUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label + "\" parameter cannot be dynamic!");
            }
        }

        // Doc-level parameter: dest_type (default: Topic)
        LongFunction<String> destTypeStrFunc = (l) -> S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label;
        if (cmdTpl.containsKey(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label)) {
            if (cmdTpl.isStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label)) {
                destTypeStrFunc = (l) -> cmdTpl.getStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label);
            } else {
                throw new RuntimeException("\"" + S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label + "\" parameter cannot be dynamic!");
            }
        }

        // Doc-level parameter: dest_name
        LongFunction<String> destNameStrFunc = (l) -> null;
        if (cmdTpl.containsKey(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label)) {
            if (cmdTpl.isStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label)) {
                destNameStrFunc = (l) -> cmdTpl.getStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label);
            } else {
                destNameStrFunc = (l) -> cmdTpl.getDynamic(S4JActivityUtil.DOC_LEVEL_PARAMS.DEST_NAME.label, l);
            }
        }

        // Doc-level parameter: reuse_producer (default: true)
        LongFunction<Boolean> reuseProducerBoolFunc = (l) -> true;
        if (cmdTpl.containsKey(S4JActivityUtil.DOC_LEVEL_PARAMS.REUSE_PRODUCER.label)) {
            if (cmdTpl.isStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.REUSE_PRODUCER.label)) {
                reuseProducerBoolFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.REUSE_PRODUCER.label));
            } else {
                throw new RuntimeException("\"" + S4JActivityUtil.DOC_LEVEL_PARAMS.REUSE_PRODUCER.label + "\" parameter cannot be dynamic!");
            }
        }

        // Doc-level parameter: async_api (default: false)
        LongFunction<Boolean> asyncAPIBoolFunc = (l) -> false;
        if (cmdTpl.containsKey(S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
            if (cmdTpl.isStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
                asyncAPIBoolFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label));
            } else {
                throw new RuntimeException("\"" + S4JActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label + "\" parameter cannot be dynamic!");
            }
        }

        if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(s4JActivity,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                reuseProducerBoolFunc,
                asyncAPIBoolFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ.label)) {
            return resolveMsgRead(s4JActivity,
                false,
                false,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBoolFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED.label)) {
            return resolveMsgRead(s4JActivity,
                false,
                true,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBoolFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_DURABLE.label)) {
            return resolveMsgRead(s4JActivity,
                true,
                false,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBoolFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED_DURABLE.label)) {
            return resolveMsgRead(s4JActivity,
                true,
                true,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBoolFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, S4JActivityUtil.MSG_OP_TYPES.MSG_BROWSE.label)) {
            return resolveMsgBrowse(s4JActivity,
                tempDestBoolFunc,
                destTypeStrFunc,
                destNameStrFunc,
                asyncAPIBoolFunc);
        } else {
            throw new RuntimeException("Unsupported JMS operation type");
        }
    }

    private LongFunction<S4JOp> resolveMsgSend(
        S4JActivity s4JActivity,
        LongFunction<Boolean> tempDestBoolFunc,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        LongFunction<Boolean> reuseProducerBoolFunc,
        LongFunction<Boolean> asyncAPIBoolFunc
    ) {
        // JMS message headers
        LongFunction<String> msgHeaderJsonStrFunc;
        if (cmdTpl.isStatic("msg_header")) {
            msgHeaderJsonStrFunc = (l) -> cmdTpl.getStatic("msg_header");
        } else if (cmdTpl.isDynamic("msg_header")) {
            msgHeaderJsonStrFunc = (l) -> cmdTpl.getDynamic("msg_property", l);
        } else {
            msgHeaderJsonStrFunc = (l) -> null;
        }

        // JMS message properties
        LongFunction<String> msgPropJsonStrFunc;
        if (cmdTpl.isStatic("msg_property")) {
            msgPropJsonStrFunc = (l) -> cmdTpl.getStatic("msg_property");
        } else if (cmdTpl.isDynamic("msg_property")) {
            msgPropJsonStrFunc = (l) -> cmdTpl.getDynamic("msg_property", l);
        } else {
            msgPropJsonStrFunc = (l) -> null;
        }

        // JMS message type
        LongFunction<String> msgTypeFunc;
        if (cmdTpl.containsKey("msg_type")) {
            if (cmdTpl.isStatic("msg_type")) {
                msgTypeFunc = (l) -> cmdTpl.getStatic("msg_type");
            } else if (cmdTpl.isDynamic("msg_type")) {
                msgTypeFunc = (l) -> cmdTpl.getDynamic("msg_type", l);
            } else {
                msgTypeFunc = (l) -> null;
            }
        } else {
            throw new RuntimeException("JMS message send:: \"msg_type\" field must be specified!");
        }

        // JMS message body
        LongFunction<String> msgBodyRawJsonStrFunc;
        if (cmdTpl.containsKey("msg_body")) {
            if (cmdTpl.isStatic("msg_body")) {
                msgBodyRawJsonStrFunc = (l) -> cmdTpl.getStatic("msg_body");
            } else if (cmdTpl.isDynamic("msg_body")) {
                msgBodyRawJsonStrFunc = (l) -> cmdTpl.getDynamic("msg_body", l);
            } else {
                msgBodyRawJsonStrFunc = (l) -> null;
            }
        } else {
            throw new RuntimeException("JMS message send:: \"msg_body\" field must be specified!");
        }

        return new S4JMsgSendMapper(
            s4JActivity,
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            reuseProducerBoolFunc,
            asyncAPIBoolFunc,
            msgHeaderJsonStrFunc,
            msgPropJsonStrFunc,
            msgTypeFunc,
            msgBodyRawJsonStrFunc);
    }

    private LongFunction<S4JOp> resolveMsgRead(
        S4JActivity s4JActivity,
        boolean durableConsumer,                    // only relevant for Topic
        boolean sharedConsumer,                     // only relevant for Topic
        LongFunction<Boolean> tempDestBoolFunc,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        LongFunction<Boolean> asyncAPIBoolFunc
    ) {
        // subscription name - only relevant for Topic
        LongFunction<String> subNameStrFunc = (l) -> null;
        if (cmdTpl.containsKey("subscription_name")) {
            if (cmdTpl.isStatic("subscription_name")) {
                subNameStrFunc = (l) -> cmdTpl.getStatic("subscription_name");
            } else {
                subNameStrFunc = (l) -> cmdTpl.getDynamic("subscription_name", l);
            }
        }

        // message selector - only relevant for Topic
        LongFunction<String> msgSelectorStrFunc = (l) -> null;
        if (cmdTpl.containsKey("msg_selector")) {
            if (cmdTpl.isStatic("msg_selector")) {
                msgSelectorStrFunc = (l) -> cmdTpl.getStatic("msg_selector");
            } else {
                throw new RuntimeException("\"" + "msg_selector" + "\" parameter cannot be dynamic!");
            }
        }

        // non local
        LongFunction<Boolean> noLocalBoolFunc = (l) -> false;
        if (cmdTpl.containsKey("no_local")) {
            if (cmdTpl.isStatic("no_local")) {
                noLocalBoolFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic("no_local"));
            } else {
                throw new RuntimeException("\"" + "no_local" + "\" parameter cannot be dynamic!");
            }
        }

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

        LongFunction<Boolean> recvNoWaitBoolFunc = (l) -> false;
        if (cmdTpl.containsKey("no_wait")) {
            if (cmdTpl.isStatic("no_wait")) {
                recvNoWaitBoolFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic("no_wait"));
            } else {
                throw new RuntimeException("\"" + "no_wait" + "\" parameter cannot be dynamic!");
            }
        }


        return new S4JMsgReadMapper(
            s4JActivity,
            durableConsumer,
            sharedConsumer,
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBoolFunc,
            subNameStrFunc,
            msgSelectorStrFunc,
            noLocalBoolFunc,
            readTimeoutFunc,
            recvNoWaitBoolFunc);
    }

    private LongFunction<S4JOp> resolveMsgBrowse(
        S4JActivity s4JActivity,
        LongFunction<Boolean> tempDestBoolFunc,
        LongFunction<String> destTypeStrFunc,
        LongFunction<String> destNameStrFunc,
        LongFunction<Boolean> asyncAPIBoolFunc
    ) {
        // message selector - only relevant for Topic
        LongFunction<String> msgSelectorStrFunc = (l) -> null;
        if (cmdTpl.containsKey("msg_selector")) {
            if (cmdTpl.isStatic("msg_selector")) {
                msgSelectorStrFunc = (l) -> cmdTpl.getStatic("msg_selector");
            } else {
                throw new RuntimeException("\"" + "msg_selector" + "\" parameter cannot be dynamic!");
            }
        }

        return new S4JMsgBrowseMapper(
            s4JActivity,
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBoolFunc,
            msgSelectorStrFunc);
    }
}
