package io.nosqlbench.driver.mongodb;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import org.bson.conversions.Bson;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadyMongoStatementTest {
    private final static Logger logger = LoggerFactory.getLogger(ReadyMongoStatementTest.class);

    private ActivityDef activityDef;
    private StmtsDocList stmtsDocList;

    @Before
    public void setup() {
        String[] params = {
                "yaml=activities/mongodb-basic.yaml",
                "database=nosqlbench_testdb",
        };
        activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
        stmtsDocList = StatementsLoader.load(logger, yaml_loc, new StrInterpolator(activityDef), "activities");
    }

    @Test
    public void testResolvePhaseRampup() {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:rampup");

        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (StmtDef stmt : stmts) {
            ParsedStmt parsed = stmt.getParsed().orError();
            assertThat(parsed.getBindPoints()).hasSize(2);

            BindPoint seqKey = new BindPoint("seq_key", "Mod(1000000000); ToString() -> String");
            BindPoint seqValue = new BindPoint("seq_value", "Hash(); Mod(1000000000); ToString() -> String");
            assertThat(parsed.getBindPoints()).containsExactly(seqKey, seqValue);

            String statement = parsed.getPositionalStatement(Function.identity());
            Objects.requireNonNull(statement);

            ReadyMongoStatement readyMongoStatement = new ReadyMongoStatement(stmt);
            Bson bsonDoc = readyMongoStatement.bind(1L);
            assertThat(bsonDoc).isNotNull();
        }
    }

    @Test
    public void testResolvePhaseMainRead() {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:main,name:main-find");

        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (StmtDef stmt : stmts) {
            ParsedStmt parsed = stmt.getParsed().orError();
            assertThat(parsed.getBindPoints()).hasSize(1);

            BindPoint rwKey = new BindPoint("rw_key", "Uniform(0,1000000000)->int; ToString() -> String");
            assertThat(parsed.getBindPoints()).containsExactly(rwKey);

            String statement = parsed.getPositionalStatement(Function.identity());
            Objects.requireNonNull(statement);

            ReadyMongoStatement readyMongoStatement = new ReadyMongoStatement(stmt);
            Bson bsonDoc = readyMongoStatement.bind(1L);
            assertThat(bsonDoc).isNotNull();
        }
    }

    @Test
    public void testResolvePhaseMainWrite() {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:main,name:main-insert");

        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (StmtDef stmt : stmts) {
            ParsedStmt parsed = stmt.getParsed().orError();
            assertThat(parsed.getBindPoints()).hasSize(2);

            BindPoint rwKey = new BindPoint("rw_key", "Uniform(0,1000000000)->int; ToString() -> String");
            BindPoint rwValue = new BindPoint("rw_value", "Hash(); Uniform(0,1000000000)->int; ToString() -> String");
            assertThat(parsed.getBindPoints()).containsExactly(rwKey, rwValue);

            String statement = parsed.getPositionalStatement(Function.identity());
            Objects.requireNonNull(statement);

            ReadyMongoStatement readyMongoStatement = new ReadyMongoStatement(stmt);
            Bson bsonDoc = readyMongoStatement.bind(1L);
            assertThat(bsonDoc).isNotNull();
        }
    }
}
