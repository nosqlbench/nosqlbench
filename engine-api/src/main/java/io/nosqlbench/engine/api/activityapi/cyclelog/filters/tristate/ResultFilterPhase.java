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

public class ResultFilterPhase implements TristateFilter<ResultReadable> {

    private final Policy matchingPolicy;
    private final Predicate<ResultReadable> cycleResultPredicate;

    public ResultFilterPhase(Predicate<ResultReadable> cycleResultPredicate, Policy matchingPolicy) {
        this.cycleResultPredicate = cycleResultPredicate;
        this.matchingPolicy = matchingPolicy;
    }

    @Override
    public Policy apply(ResultReadable cycleResult) {
        if (cycleResultPredicate.test(cycleResult)) {
            return matchingPolicy;
        }
        return Policy.Ignore;
    }
}
