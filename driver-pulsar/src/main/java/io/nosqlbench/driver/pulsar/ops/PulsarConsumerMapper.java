package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Consumer;

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
public class PulsarConsumerMapper implements LongFunction<PulsarOp> {
    private final LongFunction<Consumer<?>> consumerFunc;
    private final LongFunction<String> recvInstructions;
    private final CommandTemplate cmdTpl;

    public PulsarConsumerMapper(LongFunction<Consumer<?>> consumerFunc,
                                LongFunction<String> recvMsg,
                                CommandTemplate cmdTpl) {
        this.consumerFunc = consumerFunc;
        this.recvInstructions = recvMsg;
        this.cmdTpl = cmdTpl;
        // TODO add schema support
    }

    @Override
    public PulsarOp apply(long value) {
        return new PulsarConsumerOp((Consumer<byte[]>) consumerFunc.apply(value), recvInstructions.apply(value));
    }
}
