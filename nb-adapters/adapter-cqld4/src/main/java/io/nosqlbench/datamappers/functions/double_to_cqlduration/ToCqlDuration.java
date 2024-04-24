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

package io.nosqlbench.datamappers.functions.double_to_cqlduration;

import com.datastax.oss.driver.api.core.data.CqlDuration;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input double value into a CQL {@link CqlDuration} object,
 * by setting months to zero, and using the fractional part as part
 * of a day, assuming 24-hour days.
 */
@Categories({Category.datetime})
@ThreadSafeMapper
public class ToCqlDuration implements DoubleFunction<CqlDuration> {

    private final static double NS_PER_DAY = 1_000_000_000L * 60 * 60 * 24;

    @Override
    public CqlDuration apply(double value) {
        double fraction = value - (long) value;
        return CqlDuration.newInstance(0,(int)value,(long)(fraction*NS_PER_DAY));
    }
}
