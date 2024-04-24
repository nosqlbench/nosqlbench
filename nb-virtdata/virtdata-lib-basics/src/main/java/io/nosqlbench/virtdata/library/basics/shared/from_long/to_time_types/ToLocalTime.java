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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.function.LongFunction;

/**
 * Convert the input epoch millisecond to a {@code Java Instant}, by multiplying and then dividing
 * by the provided parameters, then convert the result to a java {@link LocalTime}.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class ToLocalTime implements LongFunction<LocalTime> {

    private final ZoneId zoneid;
    private final long spacing;
    private final long repeat_count;

    public ToLocalTime(int millis_multiplier, int millis_divisor, String zoneid) {
        this.spacing = millis_multiplier;
        this.repeat_count = millis_divisor;
        if (zoneid.equals("default")) {
            this.zoneid = ZoneId.systemDefault();
        } else {
            this.zoneid = ZoneId.of(zoneid);
        }

    }

    @Example({"ToLocalTime(86400000,2)","produce two LocalTime values per day"})
    public ToLocalTime(int millis_multiplier, int millis_divisor){
        this(millis_multiplier,millis_divisor,ZoneId.systemDefault().toString());
    }

    @Example({"ToLocalTime(86400000)","produce a single LocalTime per day"})
    public ToLocalTime(int spacing){
        this(spacing, 1, ZoneId.systemDefault().toString());
    }

    public ToLocalTime() {
        this(1,1,"default");
    }

    @Override
    public LocalTime apply(long input) {
        input = (input*spacing)/repeat_count;
        return LocalTime.ofInstant(Instant.ofEpochMilli(input), zoneid);
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + spacing+ ":" + repeat_count;
    }
}
