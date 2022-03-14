/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.datamappers.functions.long_uuid;

//import com.datastax.driver.core.utils.UUIDs;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * Converts a long timestamp in epoch millis form into a Version 1 TimeUUID
 * according to <a href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
 * This form uses {@link Uuids#endOf(long)} (long)}
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToTimeUUIDMax implements LongFunction<UUID> {

    @Override
    public UUID apply(long value) {
        return Uuids.endOf(value);
    }
}
