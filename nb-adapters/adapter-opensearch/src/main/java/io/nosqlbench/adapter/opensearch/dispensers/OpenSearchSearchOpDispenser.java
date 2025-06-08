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

package io.nosqlbench.adapter.opensearch.dispensers;

import io.nosqlbench.adapter.opensearch.OpenSearchAdapter;
import io.nosqlbench.adapter.opensearch.ops.OpenSearchBaseOp;
import io.nosqlbench.adapter.opensearch.ops.OpenSearchSearchOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class OpenSearchSearchOpDispenser extends OpenSearchBaseOpDispenser {
    private Class<?> schemaClass;

    public OpenSearchSearchOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
        String schemaClassStr = op.getStaticConfigOr("schema", "io.nosqlbench.adapter.opensearch.pojos.Doc");
        try {
            schemaClass = Class.forName(schemaClassStr);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load schema class: " + schemaClassStr, e);
        }
    }

    @Override
    public LongFunction<? extends OpenSearchBaseOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        // Build the query from the workload definition
        LongFunction<Query> queryFunc = buildQuery(op);
        
        LongFunction<SearchRequest.Builder> bfunc =
            l -> new SearchRequest.Builder()
                .size(op.getStaticValueOr("size", 10))
                .index(targetF.apply(l))
                .query(queryFunc.apply(l));

        return (long l) -> new OpenSearchSearchOp(clientF.apply(l), bfunc.apply(l).build(), schemaClass);
    }

    private LongFunction<Query> buildQuery(ParsedOp op) {
        // Get the query definition from the workload
        Map<String, Object> queryDef = op.getStaticValue("query", Map.class);
        
        if (queryDef == null) {
            // Default to match_all if no query specified
            return l -> Query.of(q -> q.matchAll(m -> m));
        }
        
        // Handle different query types
        if (queryDef.containsKey("match_all")) {
            return l -> Query.of(q -> q.matchAll(m -> m));
        } else if (queryDef.containsKey("match")) {
            Map<String, Object> matchQuery = (Map<String, Object>) queryDef.get("match");
            String field = matchQuery.keySet().iterator().next();
            String value = matchQuery.get(field).toString();
            return l -> Query.of(q -> q.match(m -> m.field(field).query(FieldValue.of(value))));
        } else if (queryDef.containsKey("term")) {
            Map<String, Object> termQuery = (Map<String, Object>) queryDef.get("term");
            String field = termQuery.keySet().iterator().next();
            String value = termQuery.get(field).toString();
            return l -> Query.of(q -> q.term(t -> t.field(field).value(FieldValue.of(value))));
        } else {
            // Default to match_all for unknown query types
            return l -> Query.of(q -> q.matchAll(m -> m));
        }
    }
}
