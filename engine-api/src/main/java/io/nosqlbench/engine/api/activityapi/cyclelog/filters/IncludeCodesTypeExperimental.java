/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters;

import io.nosqlbench.engine.api.util.SimpleConfig;
import io.nosqlbench.nb.annotations.Service;

import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * A naive implementation of set filtering on integer values.
 * For now, given the (byte) constrained data type width, a simple
 * array is used. When the type widens, this will need to use a native
 * int trie or something else that is time and space efficient.
 */
@Service(value = ExperimentalResultFilterType.class, selector = "include")
public class IncludeCodesTypeExperimental implements ExperimentalResultFilterType {

    @Override
    public IntPredicate getIntPredicate(SimpleConfig conf) {
        return new IncludeCodes(conf);
    }

    private static class IncludeCodes implements IntPredicate {
        private final int[] lut;

        public IncludeCodes(SimpleConfig conf) {
            lut=parseCodes(128, conf.getString("codes").orElseThrow(
                    () -> new RuntimeException("codes= was not provided in the int predicate config for " + IncludeCodes.this)
            ));
        }

        private int[] parseCodes(int len, String codes) {
            int[] lut = new int[len];
            int[] values = Arrays.stream(codes.split(";")).mapToInt(Integer::valueOf).toArray();

            for (int value : values) {
                if (value < 0 || value > Byte.MAX_VALUE) {
                    throw new RuntimeException("this filter only allows values in [0..127] for now.");
                }
                lut[value] = 1;
            }
            return lut;
        }

        @Override
        public boolean test(int value) {
            if (value<0) {
                return false;
            }
            return lut[value] > 0;
        }
    }

}
