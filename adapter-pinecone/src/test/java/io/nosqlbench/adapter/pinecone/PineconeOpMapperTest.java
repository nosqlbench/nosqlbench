package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.opdispensers.PineconeDeleteOpDispenser;
import io.nosqlbench.adapter.pinecone.opdispensers.PineconeQueryOpDispenser;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
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
        return new ParsedOp(opTemplate, cfg, List.of(adapter.getPreprocessor()));
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
                     filter: "value $lt 2"
                     include_values: true
                     include_metadata: true
                     query_vectors:
                       - id: 1
                         values: "1.0,2.0,3.0"
                         top_k: 8
                         namespace: "test-namespace"
                         filter: "value $lt 2"
                         sparse_values:
                           indices: "1,2,3"
                           values: "1.0,2.0,3.0"
                       - id: 2
                         values: "4.0,5.0,6.0"
                         top_k: 11
                         namespace: "test-namespace"
                         filter: "value $gt 10"
                """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeQueryOpDispenser);
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
                 filter: "value $gt 10"
            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        assert(dispenser instanceof PineconeDeleteOpDispenser);
        PineconeOp op = dispenser.apply(0);
    }

    @Test
    public void testDescribeIndexStatsOpDispenser() {

    }

    @Test
    public void testFetchOpDispenser() {

    }

    @Test
    public void testUpdateOpDispenser() {

    }

    @Test
    public void testUpsertOpDispenser() {

    }

    @Test
    public void testQueryOpDispenserComplex() {

    }



}
