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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_object;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Combinations;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.NumberNameToString;
import io.nosqlbench.virtdata.library.basics.shared.functionadapters.ToLongFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CoinFuncTest {

    // sanity checks on ctor usage only
    @Test
    public void testToLongCtor() {
        CoinFunc f = new CoinFunc(0.23, new ToLongFunction(new HashRange(0,3)), new Combinations("0-9"));
        Object r = f.apply(3L);
    }

    // sanity checks on ctor usage only
    @Test
    public void testLongFuncCtor() {
        CoinFunc f = new CoinFunc(0.23, new NumberNameToString(), new Combinations("0-9"));
        Object r = f.apply(3L);
    }

    // Uncomment this if you want to see the qualitative check
    @Disabled
    @Test
    public void testResults() {
        CoinFunc f = new CoinFunc(0.1,
                new ToLongFunction(new HashRange(0L,10L)),
                new ToLongFunction(new HashRange(10L,100L))
        );

        long[] counts = new long[100];
        for (int i = 0; i < 10000; i++) {
            Object r = f.apply((long) i);
            int value = ((Long)r).intValue();
            counts[value] = counts[value]+1;
        }
        String summary = Arrays.stream(counts).mapToObj(String::valueOf).collect(Collectors.joining(","));
        System.out.println(summary);
    }

}
