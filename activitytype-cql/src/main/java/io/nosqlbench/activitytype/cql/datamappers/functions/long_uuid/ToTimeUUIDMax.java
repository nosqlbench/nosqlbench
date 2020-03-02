package io.nosqlbench.activitytype.cql.datamappers.functions.long_uuid;

import com.datastax.driver.core.utils.UUIDs;
import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * Converts a long timestamp in epoch millis form into a Version 1 TimeUUID
 * according to <a href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
 * This form uses {@link UUIDs#startOf(long)}
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToTimeUUIDMax implements LongFunction<UUID> {
    @Override
    public UUID apply(long value) {
        return UUIDs.endOf(value);
    }
}
