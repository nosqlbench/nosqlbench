package io.nosqlbench.activitytype.cql.datamappers.functions.double_to_cqlduration;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.Duration;

import java.util.function.DoubleFunction;

/**
 * Convert the input double value into a CQL {@link Duration} object,
 * by setting months to zero, and using the fractional part as part
 * of a day, assuming 24-hour days.
 */
public class ToCqlDuration implements DoubleFunction<Duration> {

    private final static double NS_PER_DAY = 1_000_000_000L * 60 * 60 * 24;

    @Override
    public Duration apply(double value) {
        double fraction = value - (long) value;
        return Duration.newInstance(0,(int)value,(long)(fraction*NS_PER_DAY));
    }
}
