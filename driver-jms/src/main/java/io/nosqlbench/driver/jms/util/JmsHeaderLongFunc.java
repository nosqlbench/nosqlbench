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
