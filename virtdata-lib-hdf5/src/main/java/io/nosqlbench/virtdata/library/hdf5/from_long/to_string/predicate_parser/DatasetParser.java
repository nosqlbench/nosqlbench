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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser;

import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.NoopDatasetParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_cql.CqlDatasetParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_cql.JAWDatasetParser;

/**
 * This interface is used to parse the raw JSON from the HDF dataset into a CQL predicate.
 */
public interface DatasetParser {

    /**
     * Return the specified class to parse the raw JSON from the HDF dataset into a CQL predicate.
     * @param parsername
     * @return A new instance of the specified parser class.
     */
    static DatasetParser parserFactory(String parsername) {
        return switch (parsername) {
            case "cql" -> new CqlDatasetParser();
            case "noop" -> new NoopDatasetParser();
            case "jaw" -> new JAWDatasetParser();
            default -> throw new RuntimeException("Unknown parser name: " + parsername);
        };
    }

    String parse(String raw);
}
