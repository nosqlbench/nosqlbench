package io.nosqlbench.driver.jms.ops;

import io.nosqlbench.driver.jms.JmsActivity;

import javax.jms.Destination;
import java.util.function.LongFunction;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class JmsMsgReadMapper extends JmsOpMapper {

    private final LongFunction<Boolean> jmsConsumerDurableFunc;
    private final LongFunction<Boolean> jmsConsumerSharedFunc;
    private final LongFunction<String> jmsMsgSubscriptionFunc;
    private final LongFunction<String> jmsMsgReadSelectorFunc;
    private final LongFunction<Boolean> jmsMsgNoLocalFunc;
    private final LongFunction<Long> jmsReadTimeoutFunc;

    public JmsMsgReadMapper(JmsActivity jmsActivity,
                            LongFunction<Boolean> asyncApiFunc,
                            LongFunction<Destination> jmsDestinationFunc,
                            LongFunction<Boolean> jmsConsumerDurableFunc,
                            LongFunction<Boolean> jmsConsumerSharedFunc,
                            LongFunction<String> jmsMsgSubscriptionFunc,
                            LongFunction<String> jmsMsgReadSelectorFunc,
                            LongFunction<Boolean> jmsMsgNoLocalFunc,
                            LongFunction<Long> jmsReadTimeoutFunc) {
        super(jmsActivity, asyncApiFunc, jmsDestinationFunc);

        this.jmsConsumerDurableFunc = jmsConsumerDurableFunc;
        this.jmsConsumerSharedFunc = jmsConsumerSharedFunc;
        this.jmsMsgSubscriptionFunc = jmsMsgSubscriptionFunc;
        this.jmsMsgReadSelectorFunc = jmsMsgReadSelectorFunc;
        this.jmsMsgNoLocalFunc = jmsMsgNoLocalFunc;
        this.jmsReadTimeoutFunc = jmsReadTimeoutFunc;
    }

    @Override
    public JmsOp apply(long value) {
        boolean asyncApi = asyncApiFunc.apply(value);
        Destination jmsDestination = jmsDestinationFunc.apply(value);
        boolean jmsConsumerDurable = jmsConsumerDurableFunc.apply(value);
        boolean jmsConsumerShared = jmsConsumerSharedFunc.apply(value);
        String jmsMsgSubscription = jmsMsgSubscriptionFunc.apply(value);
        String jmsMsgReadSelector = jmsMsgReadSelectorFunc.apply(value);
        boolean jmsMsgNoLocal = jmsMsgNoLocalFunc.apply(value);
        long jmsReadTimeout = jmsReadTimeoutFunc.apply(value);

        // Default to NO read timeout
        if (jmsReadTimeout < 0) jmsReadTimeout = 0;

        return new JmsMsgReadOp(
            jmsActivity,
            asyncApi,
            jmsDestination,
            jmsConsumerDurable,
            jmsConsumerShared,
            jmsMsgSubscription,
            jmsMsgReadSelector,
            jmsMsgNoLocal,
            jmsReadTimeout
        );
    }
}
