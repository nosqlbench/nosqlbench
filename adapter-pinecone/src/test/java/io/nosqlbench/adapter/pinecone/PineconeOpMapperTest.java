package io.nosqlbench.adapter.pinecone;

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
        cfg = PineconeSpace.getConfigModel().apply(Map.of());
        adapter = new PineconeDriverAdapter();
        adapter.applyConfig(cfg);
        DriverSpaceCache<? extends PineconeSpace> cache = adapter.getSpaceCache();
        mapper = new PineconeOpMapper(adapter, cache, cfg);
    }

    private static ParsedOp parsedOpFor(String yaml) {
        OpsDocList docs = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, Map.of(), null);
        OpTemplate opTemplate = docs.getOps().get(0);
        ParsedOp parsedOp = new ParsedOp(opTemplate, cfg, List.of(adapter.getPreprocessor()));
        return parsedOp;
    }

    @Test
    public void testQueryOpDispenserSimple() {
        ParsedOp pop = parsedOpFor("""
                                         ops:
                                           query-op1:
                                             query: "test-index"
                                             vector: "1.0,2.0,3.0"
                                             namespace: "test-namespace"
                                             top_k: 10
                                             filters:
                                               - filter_field: "value"
                                                 operator: "$lt"
                                                 comparator: 2
                                             include_values: true
                                             include_metadata: true

            """);
        OpDispenser<? extends PineconeOp> dispenser = mapper.apply(pop);
        //assertions go here...
    }

    @Test
    public void testDeleteOpDispenser() {

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
