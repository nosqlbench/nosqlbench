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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;

import java.util.function.Predicate;

public interface CycleResultPredicate extends Predicate<ResultReadable> {

    class ResultHasSomeBits implements CycleResultPredicate {
        private final int mask;

        public ResultHasSomeBits(int mask) {
            this.mask = mask;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return ((cycleResult.getResult() & mask)>0);
        }
    }

    class ResultHasAllBits implements CycleResultPredicate {
        private final int mask;

        public ResultHasAllBits(int mask) {
            this.mask = mask;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return ((cycleResult.getResult() & mask) == mask);
        }
    }

    class ResultInRange implements CycleResultPredicate {

        private final int min;
        private final int max;

        public ResultInRange(int minInclusive, int maxExclusive) {
            this.min = minInclusive;
            this.max = maxExclusive;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return (min<=cycleResult.getResult() && max>=cycleResult.getResult());
        }
    }

    class ResultEquals implements CycleResultPredicate {
        private final int value;

        public ResultEquals(int value) {
            this.value = value;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return cycleResult.getResult() == value;
        }
    }
}
