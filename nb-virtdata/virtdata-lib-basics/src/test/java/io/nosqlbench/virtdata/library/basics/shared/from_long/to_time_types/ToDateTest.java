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

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ToDateTest {

    @Test
    public void testToDateSimple() {
        ToDate f = new ToDate(5);
        assertThat(f.apply(0)).isEqualTo(new Date(0));
        assertThat(f.apply(1)).isEqualTo(new Date(5));
        assertThat(f.apply(100)).isEqualTo(new Date(500));
    }

    @Test
    public void testToDateWithRepeats() {
        ToDate f = new ToDate(86400000,2);
        assertThat(f.apply(0)).isEqualTo(new Date(0));
        assertThat(f.apply(1)).isEqualTo(new Date((86400000/2)));
        assertThat(f.apply(2)).isEqualTo(new Date((2*86400000)/2));
        assertThat(f.apply(3)).isEqualTo(new Date((3*86400000)/2));
    }

}
