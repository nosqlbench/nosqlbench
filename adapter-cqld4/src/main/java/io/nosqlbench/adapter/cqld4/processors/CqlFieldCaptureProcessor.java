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

package io.nosqlbench.adapter.cqld4.processors;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;
import io.nosqlbench.virtdata.core.templates.CapturePoint;

import java.util.List;

public class CqlFieldCaptureProcessor implements ResultSetProcessor {

    private final List<CapturePoint> captures;

    public CqlFieldCaptureProcessor(List<CapturePoint> captures) {
        this.captures = captures;
    }

    @Override
    public void start(long cycle, ResultSet container) {

    }

    @Override
    public void buffer(Row element) {

    }

    @Override
    public void flush() {

    }
}
