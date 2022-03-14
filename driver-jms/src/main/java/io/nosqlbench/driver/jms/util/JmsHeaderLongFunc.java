/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.driver.jms.util;

import lombok.*;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import java.util.function.LongFunction;

@Setter
@Getter
@NoArgsConstructor
public class JmsHeaderLongFunc implements LongFunction {
    private LongFunction<Integer> deliveryModeFunc;
    private LongFunction<Integer> msgPriorityFunc;
    private LongFunction<Long> msgTtlFunc;
    private LongFunction<Long> msgDeliveryDelayFunc;
    private LongFunction<Boolean> disableMsgTimestampFunc;
    private LongFunction<Boolean> disableMsgIdFunc;

    @Override
    public Object apply(long value) {
        return new JmsHeader(
            (deliveryModeFunc != null) ? deliveryModeFunc.apply(value) : DeliveryMode.PERSISTENT,
            (msgPriorityFunc != null) ? msgPriorityFunc.apply(value) : Message.DEFAULT_PRIORITY,
            (msgTtlFunc != null) ? msgTtlFunc.apply(value) : Message.DEFAULT_TIME_TO_LIVE,
            (msgTtlFunc != null) ? msgTtlFunc.apply(value) : Message.DEFAULT_DELIVERY_DELAY,
            (disableMsgTimestampFunc != null) ? disableMsgTimestampFunc.apply(value) : false,
            (disableMsgIdFunc != null) ? disableMsgIdFunc.apply(value) : false
        );
    }
}
