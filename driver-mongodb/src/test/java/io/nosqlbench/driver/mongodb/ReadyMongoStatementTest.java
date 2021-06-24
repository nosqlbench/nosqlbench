package io.nosqlbench.driver.mongodb;

import io.nosqlbench.engine.api.activityconfig.ParsedStmtOp;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadyMongoStatementTest {
    private final static Logger logger = LogManager.getLogger(ReadyMongoStatementTest.class);

    private ActivityDef activityDef;
    private StmtsDocList stmtsDocList;

    @BeforeEach
    public void setup() {
        String[] params = {
                "yaml=activities/mongodb-basic.yaml",
                "database=nosqlbench_testdb",
        };
        activityDef = ActivityDef.parseActivityDef(String.join(";", params));
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
        stmtsDocList = StatementsLoader.loadPath(logger, yaml_loc, new StrInterpolator(activityDef), "activities");
    }

    @Test
    public void testResolvePhaseRampup() {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:rampup");

        List<OpTemplate> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (OpTemplate stmt : stmts) {
            ParsedStmtOp parsed = stmt.getParsed().orElseThrow();
            assertThat(parsed.getBindPoints()).hasSize(2);

            BindPoint seqKey = new BindPoint("seq_key", "Mod(1000000L); ToInt()");
            BindPoint seqValue = new BindPoint("seq_value", "Mod(1000000000L); Hash(); ToString() -> String");
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
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:main,name:.*main-find");

        List<OpTemplate> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (OpTemplate stmt : stmts) {
            ParsedStmtOp parsed = stmt.getParsed().orElseThrow();
            assertThat(parsed.getBindPoints()).hasSize(1);

            BindPoint rwKey = new BindPoint("rw_key", "Uniform(0,1000000)->long; ToInt()");
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
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:main,name:.*main-insert");

        List<OpTemplate> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (OpTemplate stmt : stmts) {
            ParsedStmtOp parsed = stmt.getParsed().orElseThrow();
            assertThat(parsed.getBindPoints()).hasSize(2);

            BindPoint rwKey = new BindPoint("rw_key", "Uniform(0,1000000)->long; ToInt()");
            BindPoint rwValue = new BindPoint("rw_value", "Uniform(0,1000000000)->int; Hash(); ToString() -> String");
            assertThat(parsed.getBindPoints()).containsExactly(rwKey, rwValue);

            String statement = parsed.getPositionalStatement(Function.identity());
            Objects.requireNonNull(statement);

            ReadyMongoStatement readyMongoStatement = new ReadyMongoStatement(stmt);
            Bson bsonDoc = readyMongoStatement.bind(1L);
            assertThat(bsonDoc).isNotNull();
        }
    }
}
