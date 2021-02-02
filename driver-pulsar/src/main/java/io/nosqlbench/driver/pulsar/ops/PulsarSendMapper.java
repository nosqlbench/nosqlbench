package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;

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
public class PulsarSendMapper implements LongFunction<PulsarOp> {
    private final CommandTemplate cmdTpl;
    private final LongFunction<Producer<?>> producerFunc;
    private final LongFunction<String> payloadFunc;

    public PulsarSendMapper(
        LongFunction<Producer<?>> producerFunc,
        LongFunction<String> msgFunc,
        CommandTemplate cmdTpl) {
        this.producerFunc = producerFunc;
        this.payloadFunc = msgFunc;
        this.cmdTpl = cmdTpl;
        // TODO: add schema support
    }

    @Override
    public PulsarOp apply(long value) {
        Producer<?> producer = producerFunc.apply(value);
        String msg = payloadFunc.apply(value);
        return new PulsarSendOp((Producer<byte[]>) producer, msg);
    }
}
