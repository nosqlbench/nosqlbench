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

package io.nosqlbench.datamappers.functions.long_to_cqlduration;

import com.datastax.oss.driver.api.core.data.CqlDuration;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input value into a {@link CqlDuration}
 * by reading the input as total nanoseconds, assuming 30-month days.
 */
@ThreadSafeMapper
@Categories({Category.conversion,Category.datetime})
public class ToCqlDurationNanos implements LongFunction<CqlDuration> {

    private final static long NS_PER_S = 1_000_000_000L;
    private final static long NS_PER_DAY = NS_PER_S * 60*60*24;
    private final static long NS_PER_MONTH = NS_PER_DAY * 30;

    @Override
    public CqlDuration apply(long value) {
        long nanos = value % NS_PER_DAY;
        value -= nanos;
        long days = value / NS_PER_DAY;
        value -= days*NS_PER_DAY;
        long months = value / NS_PER_MONTH;
        return CqlDuration.newInstance((int) months,(int) days, nanos);
    }
}
