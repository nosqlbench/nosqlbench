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

package io.nosqlbench.datamappers.functions.long_localdate;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class EpochMillisToJavaLocalDateTest {

    @Test
    public void testDayAt2020Start() {
        EpochMillisToJavaLocalDate func = new EpochMillisToJavaLocalDate();
        LocalDate v1 = func.apply(0);
        LocalDate zerodate = LocalDate.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
        assertThat(v1).isEqualTo(zerodate);

        LocalDate v2 = func.apply(533664000002000L);
        DateTime dt2 = new DateTime(533664000002000L);
        LocalDate d2instant = LocalDate.ofInstant(Instant.ofEpochMilli(533664000002000L), ZoneId.systemDefault());
        assertThat(v2).isEqualTo(d2instant);
    }

}
