/*
 * Copyright (c) 2023-2024 nosqlbench
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
 *
 */

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_cql;

import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetParser;

/**
 * This class is used to parse the raw JSON from the HDF dataset into a CQL predicate. This implementation
 * accepts a string consisting of the desired CQL predicate as translated from the original jsonl files
 * and simply adds the WHERE keyword to the beginning of the string if it is not already present, hence
 * the new Just Add Where (JAW) parser.
 */
public class JAWDatasetParser implements DatasetParser {
    private static final String WHERE = "WHERE";
    @Override
    public String parse(String raw) {
        if (!raw.toUpperCase().startsWith(WHERE)) {
            raw = WHERE + " " + raw;
        }
        return raw;
    }
}
