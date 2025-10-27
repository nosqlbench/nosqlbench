/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.schema;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility for computing label set hashes compatible with SqliteSnapshotReporter.
 * This logic is intentionally duplicated from SqliteSnapshotReporter to maintain
 * module independence (nb-api doesn't depend on nb-mql-api).
 */
public class LabelSetResolver {

    /**
     * Compute a deterministic hash string for a label set.
     * Format: {key1=value1,key2=value2,...} with keys sorted alphabetically.
     *
     * @param labels Map of label key-value pairs
     * @return Hash string representation
     */
    public static String computeHash(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return "{}";
        }
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        labels.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
        return joiner.toString();
    }

    private LabelSetResolver() {
        // Utility class - prevent instantiation
    }
}
