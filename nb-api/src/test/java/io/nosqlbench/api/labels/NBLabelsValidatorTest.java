/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.labels;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NBLabelsValidatorTest {

    @Test
    public void testNoViolations() {
        NBLabelsValidator lv = new NBLabelsValidator("+needed -disallowed indifferent");
        assertThat(lv.apply(NBLabels.forKV("needed","haveit","indifferent","isalsoallowed")))
                .isEqualTo(NBLabels.forKV("needed","haveit","indifferent","isalsoallowed"));
    }

    @Test
    public void testExtraneousViolations() {
        NBLabelsValidator lv = new NBLabelsValidator("+needed -disallowed indifferent");
        assertThrows(RuntimeException.class, () -> lv.apply(NBLabels.forKV("needed","haveit","indifferent","isalsoallowed","disallowed","neversaynever")));
    }

    @Test
    public void TestMissingViolations() {
        NBLabelsValidator lv = new NBLabelsValidator("+needed -disallowed indifferent");
        assertThrows(RuntimeException.class, () -> lv.apply(NBLabels.forKV("indifferent","isalsoallowed","unrelated","events")));

    }

}
