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

package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.opdispensers.*;
import io.nosqlbench.adapter.pinecone.ops.*;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class PineconeOpMapperTest {

    private final static Logger logger = LogManager.getLogger(PineconeOpMapperTest.class);
    static NBConfiguration cfg;
    static PineconeDriverAdapter adapter;
    static PineconeOpMapper mapper;

    @BeforeAll
    public static void initializeTestMapper() {
        Map<String,String> configMap = Map.of("apiKey","2f55b2f0-670f-4c51-9073-4d37142b761a",
            "environment","us-east-1-aws",
            "projectName","default");
        cfg = PineconeSpace.getConfigModel().apply(configMap);
        adapter = new PineconeDriverAdapter();
        adapter.applyConfig(cfg);
        DriverSpaceCache<? extends PineconeSpace> cache = adapter.getSpaceCache();
        mapper = new PineconeOpMapper(adapter, cache, cfg);
    }

    private static ParsedOp parsedOpFor(String yaml) {
        OpsDocList docs = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, Map.of(), null);
        OpTemplate opTemplate = docs.getOps().get(0);
        NBLabeledElement parent = NBLabeledElement.EMPTY;
        return new ParsedOp(opTemplate, cfg, List.of(adapter.getPreprocessor()), parent);
    }

    @Test
    public void testQueryOpDispenserSimple() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                 type: "query"
                 index: "test-index"
                 vector: "1.0,2.0,3.0"
                 namespace: "test-namespace"
                 top_k: 10
                 filter:
                   filterfield: "field"
                   operator: "$gt"
                   comparator: 2.0
                 include_values: true
                 include_metadata: true
                 query_vectors:
                   - id: 1
                     values: "1.0,2.0,3.0"
                     top_k: 8
                     namespace: "test-namespace"
                     filter:
                       filterfield: "field"
                       operator: "$lt"
                       comparator: 9.0
                     sparse_values:
                       indices: "1,2,3"
                       values: "1.0,2.0,3.0"
                   - id: 2
                     values: "4.0,5.0,6.0"
                     top_k: 11
                     namespace: "test-namespace"
                     filter:
                       filterfield: "field"
                       operator: "$eq"
                       comparator: "val"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeQueryOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeQueryOp);
    }

    @Test
    public void testDeleteOpDispenser() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                 type: "delete"
                 index: "test-index"
                 ids: "1.0,2.0,3.0"
                 namespace: "test-namespace"
                 deleteall: true
                 filter:
                   filterfield: "key"
                   operator: "$eq"
                   comparator: "val"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeDeleteOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeDeleteOp);
    }

    @Test
    public void testDescribeIndexStatsOpDispenser() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                 type: "describeindexstats"
                 index: "test-index"
                 filter:
                   filterfield: "color"
                   operator: "$eq"
                   comparator:
                     - "green"
                     - "yellow"
                     - "red"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeDescribeIndexStatsOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeDescribeIndexStatsOp);

    }

    @Test
    public void testFetchOpDispenser() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                 type: "fetch"
                 index: "test-index"
                 ids: "1.0,2.0,3.0"
                 namespace: "test-namespace"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeFetchOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeFetchOp);

    }

    @Test
    public void testUpdateOpDispenser() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                 type: "update"
                 index: "test-index"
                 id: "id"
                 values: "1.0,2.0,3.0"
                 namespace: "test_namespace"
                 metadata:
                  key1: "val1"
                  key2: 2
                  key3: 3
                 sparse_values:
                  indices: "1,2,3"
                  values: "1.1,2.2,3.3"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeUpdateOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeUpdateOp);

    }

    @Test
    public void testUpsertOpDispenser() {
        ParsedOp pop = parsedOpFor("""
            ops:
              op1:
                type: "upsert"
                index: "test-index"
                upsert_vectors:
                 - id: 1
                   values:
                     - 1.0
                     - 2.0
                     - 3.0
                   sparse_values:
                     indices: "1,2,3"
                     values: "4.0,5.0,6.0"
                   metadata:
                     key1: "val1"
                     key2: 2
                 - id: 2
                   values:
                     - 7.0
                     - 8.0
                     - 9.0
                   sparse_values:
                     indices: "4,5,6"
                     values: "1.1,2.2,3.3"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeUpsertOpDispenser);
        PineconeOp op = dispenser.apply(0);
        assert(op instanceof PineconeUpsertOp);

    }

}
