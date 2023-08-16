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

package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapters.api.activityimpl.uniform.ResultProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RSProcessors implements ResultProcessor<ResultSet,Row>, Supplier<List<ResultSetProcessor>> {

    private final List<Supplier<ResultSetProcessor>> suppliers = new ArrayList<>();

    private final ThreadLocal<List<ResultSetProcessor>> processors = ThreadLocal.withInitial(this);

    @Override
    public List<ResultSetProcessor> get() {
        return suppliers.stream().map(Supplier::get).collect(Collectors.toList());
    }


    public List<Supplier<ResultSetProcessor>> getProcessors() {
        return suppliers;
    }

    public RSProcessors add(Supplier<ResultSetProcessor> processor) {
        suppliers.add(processor);
        return this;
    }

    @Override
    public void start(long cycle, ResultSet container) {
        for (ResultSetProcessor processor : get()) {
            processor.start(cycle, container);
        }
    }

    @Override
    public void buffer(Row element) {
        for (ResultSetProcessor processor : get()) {
            processor.buffer(element);
        }
    }

    @Override
    public void flush() {
        for (ResultSetProcessor processor : get()) {
            processor.flush();
        }
    }

}
