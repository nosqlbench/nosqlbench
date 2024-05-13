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
 */

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertManyOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiInsertManyOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class DataApiInsertManyOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiInsertManyOpDispenser.class);
    private final LongFunction<DataApiInsertManyOp> opFunction;

    public DataApiInsertManyOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiInsertManyOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            List<Document> documents = new ArrayList<>();
            op.getAsRequiredFunction("documents", List.class).apply(l).forEach(o -> documents.add(Document.parse(o.toString())));
            return new DataApiInsertManyOp(
                spaceFunction.apply(l).getDatabase(),
                targetFunction.apply(l),
                documents,
                getInsertManyOptions(op, l)
            );
        };
    }

    private InsertManyOptions getInsertManyOptions(ParsedOp op, long l) {
        InsertManyOptions options = new InsertManyOptions();
        Optional<LongFunction<Map>> optionsFunction = op.getAsOptionalFunction("options", Map.class);
        if (optionsFunction.isPresent()) {
            Map<String, String> optionFields = optionsFunction.get().apply(l);
            for(Map.Entry<String,String> entry: optionFields.entrySet()) {
                switch(entry.getKey()) {
                    case "chunkSize"->
                        options = options.chunkSize(Integer.parseInt(entry.getValue()));
                    case "concurrency" ->
                        options = options.concurrency(Integer.parseInt(entry.getValue()));
                    case "ordered" ->
                        options = options.ordered(Boolean.parseBoolean(entry.getValue()));
                }
            }
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
