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

package io.nosqlbench.datamappers.functions.to_daterange;

import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class DateRangeFuncTest {

    @Test
    public void testDateRangeFuncs() {
        LongFunction<Long> lf1 = value -> value;
        DateRangeFunc function = new DateRangeFunc("second", lf1, lf1);

        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

        LongUnaryOperator lf2 = value -> value;

        function = new DateRangeFunc("second", lf2, lf2);
        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

        Function<Long,Long> lf3 = value -> value;

        function = new DateRangeFunc("second", lf3, lf3);
        assertThat(function.apply(42L).toString())
            .isEqualTo("[1970-01-01T00:00:00 TO 1970-01-01T00:00:00]");
        assertThat(function.apply(42000L).toString())
            .isEqualTo("[1970-01-01T00:00:42 TO 1970-01-01T00:00:42]");
        assertThat(function.apply(42000000L).toString())
            .isEqualTo("[1970-01-01T11:40:00 TO 1970-01-01T11:40:00]");
        assertThat(function.apply(42000000000L).toString())
            .isEqualTo("[1971-05-02T02:40:00 TO 1971-05-02T02:40:00]");
        assertThat(function.apply(42000000000000L).toString())
            .isEqualTo("[3300-12-05T02:40:00 TO 3300-12-05T02:40:00]");

    }

}
