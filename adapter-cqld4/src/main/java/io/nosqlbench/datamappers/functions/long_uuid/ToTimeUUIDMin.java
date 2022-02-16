package io.nosqlbench.datamappers.functions.long_uuid;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * Converts a long timestamp in epoch millis form into a Version 1 TimeUUID
 * according to <a href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
 * This form uses {@link Uuids#startOf(long)}
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToTimeUUIDMin implements LongFunction<UUID> {
    @Override
    public UUID apply(long value) {
        return Uuids.startOf(value);
    }
}
