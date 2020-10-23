package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Split the value range of Java longs into a number of offsets,
 * starting with Long.MIN_VALUE. This method makes it easy to construct
 * a set of offsets for testing, or to limit the values used a subset.
 * The outputs will range from Long.MIN_VALUE (-2^63) up.
 *
 * This is not an exactly emulation of token range splits in Apache Cassandra.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class PartitionLongs implements LongUnaryOperator {

    // Number of partitions to split the value range into
    private final int partitions;
    private final long[] starts;

    public PartitionLongs(int partitions) {
        this.partitions = partitions;
        if (partitions < 1) {
            throw new RuntimeException("partitions must be at least 1");
        } else if (partitions == 1) {
            this.starts = new long[]{Long.MIN_VALUE};
        } else {
            this.starts = new long[partitions];

            // because the full interval is signed and Long.MIN_VALUE is 0x8000.0000.0000.0000
            long half_interval = Long.MIN_VALUE / partitions;

            for (int i = 0; i < starts.length; i++) {
                // scaling the interval to whole interval would overflow for 2 partitions
                starts[i] = Long.MIN_VALUE + ((i * half_interval) + (i * half_interval));
            }
        }
    }

    /**
     * This method uses a divisor value with a maximum value of 2^63
     *
     * @param operand
     * @return
     */
    @Override
    public long applyAsLong(long operand) {
        int idx = (int) (operand % starts.length);
        return starts[idx];
    }
}
