package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Schema;

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
    private final CommandTemplate cmdTpl;
    private final Schema<?> pulsarSchema;
    private final LongFunction<Consumer<?>> consumerFunc;

    public PulsarConsumerMapper(CommandTemplate cmdTpl,
                                Schema<?> pulsarSchema,
                                LongFunction<Consumer<?>> consumerFunc) {
        this.cmdTpl = cmdTpl;
        this.pulsarSchema = pulsarSchema;
        this.consumerFunc = consumerFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Consumer<?> consumer = consumerFunc.apply(value);
        return new PulsarConsumerOp(consumer, pulsarSchema);
    }
}
