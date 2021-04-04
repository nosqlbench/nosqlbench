package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.Set;
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
public abstract class PulsarAdminMapper extends PulsarOpMapper {
    protected final LongFunction<Boolean> adminDelOpFunc;

    protected PulsarAdminMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Boolean> adminDelOpFunc) {
        super(cmdTpl, clientSpace, asyncApiFunc);
        this.adminDelOpFunc = adminDelOpFunc;
    }
}
