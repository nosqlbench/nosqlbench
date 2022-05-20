package io.nosqlbench.driver.mongodb;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
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
        stmtsDocList = StatementsLoader.loadPath(logger, yaml_loc, activityDef.getParams(), "activities");
    }

    @Test
    public void testResolvePhaseRampup() {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("phase:rampup");

        List<OpTemplate> stmts = stmtsDocList.getStmts(tagfilter);
        assertThat(stmts).hasSize(1);
        for (OpTemplate stmt : stmts) {
            ParsedTemplate parsed = stmt.getParsed().orElseThrow();
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
            ParsedTemplate parsed = stmt.getParsed().orElseThrow();
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
            ParsedTemplate parsed = stmt.getParsed().orElseThrow();
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
