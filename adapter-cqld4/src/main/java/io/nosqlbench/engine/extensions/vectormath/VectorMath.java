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

package io.nosqlbench.engine.extensions.vectormath;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.shaded.guava.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VectorMath {
    public double computeRecall(List<Row> rows, List<Long> expectedRowIds) {
        Set<String> found = rows.stream().map(r -> r.getString("key")).collect(Collectors.toSet());
        Set<String> expected = expectedRowIds.stream().map(String::valueOf).collect(Collectors.toSet());
        return ((double)Sets.intersection(found,expected).size()/(double)expected.size());
    }
}
