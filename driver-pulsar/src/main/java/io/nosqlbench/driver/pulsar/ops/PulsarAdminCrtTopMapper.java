package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
public class PulsarAdminCrtTopMapper extends PulsarOpMapper {
    private final LongFunction<String> topicUriFunc;
    private final LongFunction<String> enablePartionFunc;
    private final LongFunction<String> partitionNumFunc;

    public PulsarAdminCrtTopMapper(CommandTemplate cmdTpl,
                                   PulsarSpace clientSpace,
                                   LongFunction<String> topicUriFunc,
                                   LongFunction<String> enablePartionFunc,
                                   LongFunction<String> partitionNumFunc) {
        super(cmdTpl, clientSpace);
        this.topicUriFunc = topicUriFunc;
        this.enablePartionFunc = enablePartionFunc;
        this.partitionNumFunc = partitionNumFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        String topicUri = topicUriFunc.apply(value);
        String enablePartitionStr = enablePartionFunc.apply(value);
        String partitionNumStr = partitionNumFunc.apply(value);

        if ( StringUtils.isBlank(topicUri) ) {
            throw new RuntimeException("\"topic_uri\" parameter can't be empty when creating a Pulsar topic!");
        }

        boolean partitionTopic = BooleanUtils.toBoolean(enablePartitionStr);

        boolean invalidPartStr;
        int partitionNum = 0;
        if ( StringUtils.isBlank(partitionNumStr) || !StringUtils.isNumeric(partitionNumStr) ) {
            invalidPartStr = true;
        } else {
            partitionNum = Integer.valueOf(partitionNumStr);
            invalidPartStr = (partitionNum <= 0);
        }
        if (partitionTopic && invalidPartStr) {
            throw new RuntimeException("Invalid specified value for \"partition_num\" parameter when creating partitioned topic!");
        }

        return new PulsarAdminCrtTopOp(
            clientSpace,
            topicUri,
            partitionTopic,
            partitionNum);
    }
}
