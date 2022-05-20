package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

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


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberNameToStringTest {

    @Test
    public void testLargeNumbers() {
        NumberNameToString fmt = new NumberNameToString();
        assertThat(fmt.apply(Integer.MAX_VALUE))
            .isEqualTo(
                "two billion one hundred and forty seven million four hundred and eighty three thousand six hundred and forty seven"
            );
        assertThat(fmt.apply(999999999))
            .isEqualTo(
                "nine hundred and ninety nine million nine hundred and ninety nine thousand nine hundred and ninety nine"
            );
        assertThat(fmt.apply(1000000000L)).isEqualTo("one billion");
        assertThat(fmt.apply(-1000000000L)).isEqualTo("negative one billion");
        assertThat(fmt.apply(10000000000L)).isEqualTo("ten billion");
        // 9, 223,372,036, 854,775,807
        assertThat(fmt.apply(Long.MAX_VALUE)).isEqualTo("nine quintillion two hundred and twenty three quadrillion three hundred and seventy two trillion and thirty six billion eight hundred and fifty four million seven hundred and seventy five thousand eight hundred and seven");
    }

}
