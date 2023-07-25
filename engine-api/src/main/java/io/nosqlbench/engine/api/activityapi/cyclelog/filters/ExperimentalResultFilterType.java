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
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.api.spi.SimpleServiceLoader;

import java.util.function.IntPredicate;

public interface ExperimentalResultFilterType {

    SimpleServiceLoader<ExperimentalResultFilterType> FINDER =
        new SimpleServiceLoader<>(ExperimentalResultFilterType.class, Maturity.Any);

    default IntPredicateDispenser getFilterDispenser(Activity activity) {
        SimpleConfig conf = new SimpleConfig(activity, "resultfilter");
        return getFilterDispenser(conf);
    }

    default IntPredicateDispenser getFilterDispenser(SimpleConfig conf) {
        IntPredicate intPredicate = getIntPredicate(conf);
        return new StaticDispenser(intPredicate);
    }

    IntPredicate getIntPredicate(SimpleConfig conf);

    class StaticDispenser implements IntPredicateDispenser {

        private final IntPredicate predicate;

        public StaticDispenser(IntPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public IntPredicate getIntPredicate(int slot) {
            return predicate;
        }
    }
}
