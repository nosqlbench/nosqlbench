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

package io.nosqlbench.virtdata.library.basics.tests.long_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedFileExtractToString;
import org.junit.jupiter.api.Test;

import java.util.IntSummaryStatistics;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class HashedFileExtractToStringTest {

    @Test
    public void testHashedFileBasic() {
        HashedFileExtractToString extract =
            new HashedFileExtractToString("data/lorem_ipsum_full.txt", 3, 3000);
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (long cycle = 0; cycle < 50000; cycle++) {
            String apply = extract.apply(cycle);
            iss.accept(apply.length());
            assertThat(apply.length()).isGreaterThanOrEqualTo(3);
            assertThat(apply.length()).isLessThanOrEqualTo(3000);
        }

        System.out.println("Loaded examples from data/lorem_ipsum_full.txt:" + iss);
    }

    @Test
    public void testHashedFileFunction() {
        HashedFileExtractToString extract =
            new HashedFileExtractToString("data/lorem_ipsum_full.txt", (LongUnaryOperator) ((long f) -> 32734 * f));
        IntSummaryStatistics iss = new IntSummaryStatistics();

        for (long cycle = 0; cycle < 50000; cycle++) {
            String apply = extract.apply(cycle);
            iss.accept(apply.length());
        }

        System.out.println("Loaded examples from data/lorem_ipsum_full.txt:" + iss);
    }
}
