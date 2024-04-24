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

package io.nosqlbench.adapter.cqld4.processors;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;

import java.util.ArrayList;

/**
 * An accumulator for rows, sized to a page of results.
 */
public class RSIterableCapture implements ResultSetProcessor {

    private long cycle;
    private final ArrayList<Row> rows = new ArrayList<>();

    @Override
    public void start(long cycle, AsyncResultSet container) {
        this.cycle = cycle;
    }

    @Override
    public void buffer(Row element) {
        rows.add(element);

    }

    @Override
    public void flush() {

    }
}
