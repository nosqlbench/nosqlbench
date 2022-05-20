package io.nosqlbench.driver.jms.util;

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


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import javax.jms.DeliveryMode;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class JmsHeader  {
    private int deliveryMode;
    private int msgPriority;
    private long msgTtl;
    private long msgDeliveryDelay;
    private boolean disableMsgTimestamp;
    private boolean disableMsgId;

    public boolean isValidDeliveryMode() {
        return (deliveryMode == DeliveryMode.NON_PERSISTENT) || (deliveryMode == DeliveryMode.PERSISTENT);
    }

    public boolean isValidPriority() {
        return (msgPriority >= 0) && (msgPriority <= 9);
    }

    public boolean isValidTtl() {
        return msgTtl >= 0;
    }

    public boolean isValidDeliveryDelay() {
        return msgTtl >= 0;
    }

    public boolean isValidHeader() {
        return isValidDeliveryMode()
            && isValidPriority()
            && isValidTtl()
            && isValidDeliveryDelay();
    }

    public String getInvalidJmsHeaderMsgText() {
        StringBuilder sb = new StringBuilder();

        if (!isValidDeliveryMode())
            sb.append("delivery mode - " + deliveryMode + "; ");
        if (!isValidPriority())
            sb.append("message priority - " + msgPriority + "; ");
        if (!isValidTtl())
            sb.append("message TTL - " + msgTtl + "; ");
        if (!isValidDeliveryDelay())
            sb.append("message delivery delay - " + msgDeliveryDelay + "; ");

        String invalidMsgText = sb.toString();
        if (StringUtils.length(invalidMsgText) > 0)
            invalidMsgText = StringUtils.substringBeforeLast(invalidMsgText, ";");
        else
            invalidMsgText = "none";

        return "Invalid JMS header values: " + invalidMsgText;
    }
}
