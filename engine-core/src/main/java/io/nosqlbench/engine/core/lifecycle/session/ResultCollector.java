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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.lifecycle.ExecutionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResultCollector implements Consumer<ResultContext> {
    private final List<ResultContext> results = new ArrayList<>();

    @Override
    public void accept(ResultContext resultContext) {
        this.results.add(resultContext);
    }

    public ExecutionResult toExecutionResult() {
        if (results.size()==1) {
            return results.get(0).toExecutionResult();
        } else {
            long min = results.stream().mapToLong(ResultContext::startMillis).min().orElseThrow();
            long max = results.stream().mapToLong(ResultContext::stopMillis).max().orElseThrow();
            String buf = results.stream().map(ResultContext::output).collect(Collectors.joining("\n\n", "---", "--"));
            return new ExecutionResult(min,max,buf,results.get(0).getException());
        }

    }
}
