/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_time_types.from_joda_datetime;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToJavaInstant implements Function<DateTime, Instant> {

    @Override
    public Instant apply(DateTime jodaDateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(jodaDateTime.getMillis()),
            ZoneId.of(jodaDateTime.getZone().getID())
        );
        return zonedDateTime.toInstant();
    }
}
