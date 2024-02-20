/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_pineconefilter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.FilteredDatasetParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeFilterParser implements FilteredDatasetParser {
    private static final String CONDITIONS = "conditions";
    private DatasetFilter filter;
    private static final Logger logger = LogManager.getLogger(PineconeFilterParser.class);
    @Override
    public String parse(String raw) {
        logger.debug(() -> "Parsing: " + raw);
        JsonObject preconditions = JsonParser.parseString(raw).getAsJsonObject();
        JsonObject conditions = preconditions.has(CONDITIONS) ?
            preconditions.get(CONDITIONS).getAsJsonObject() :
            preconditions;
        return filter.applyFilter(conditions);
    }

    @Override
    public void setFilter(DatasetFilter filter) {
        this.filter = filter;
    }
}
