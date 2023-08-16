/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OpVariationTests {

    private final static Logger logger = LogManager.getLogger(OpVariationTests.class);

    @Test
    public void testListOpsOnly() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList docs = ysl.loadString(
            "ops:\n" +
                " - first op\n" +
                " - second op\n"
        );

        assertThat(docs.getOpsDocs()).hasSize(1);
        RawOpsDoc doc = docs.getOpsDocs().get(0);
        assertThat(doc.getRawOpDefs()).hasSize(2);
        List<RawOpDef> ops = doc.getRawOpDefs();
        RawOpDef s0 = ops.get(0);
        assertThat(s0.getName()).isEqualTo("stmt1");
        assertThat(s0.getStmt()).isEqualTo("first op");
        RawOpDef s1 = ops.get(1);
        assertThat(s1.getName()).isEqualTo("stmt2");
        assertThat(s1.getStmt()).isEqualTo("second op");
    }

    @Test
    public void testSingleEntryMapStmtsOnly() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList docs = ysl.loadString(
            "ops:\n" +
                " - s1: op one\n" +
                " - s2: op two\n"
        );
        assertThat(docs.getOpsDocs()).hasSize(1);
        RawOpsDoc doc = docs.getOpsDocs().get(0);
        assertThat(doc.getRawOpDefs()).hasSize(2);
        List<RawOpDef> ops = doc.getRawOpDefs();
        assertThat(ops.get(0)).isOfAnyClassIn(RawOpDef.class);
        assertThat(ops.get(0).getName()).isEqualTo("s1");
        assertThat(ops.get(0).getStmt()).isEqualTo("op one");
        assertThat(ops.get(1)).isOfAnyClassIn(RawOpDef.class);
        assertThat(ops.get(1).getName()).isEqualTo("s2");
        assertThat(ops.get(1).getStmt()).isEqualTo("op two");
    }

    @Test
    public void testMapStmtsOnly() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList docs = ysl.loadString(
            "ops:\n" +
                " - name: s1\n" +
                "   stmt: op one\n" +
                " - name: s2\n" +
                "   stmt: op two\n"
        );
        assertThat(docs.getOpsDocs()).hasSize(1);
        RawOpsDoc doc = docs.getOpsDocs().get(0);
        assertThat(doc.getRawOpDefs()).hasSize(2);
        List<RawOpDef> stmts = doc.getRawOpDefs();
        assertThat(stmts.get(0)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(0).getName()).isEqualTo("s1");
        assertThat(stmts.get(0).getStmt()).isEqualTo("op one");
        assertThat(stmts.get(1)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(1).getName()).isEqualTo("s2");
        assertThat(stmts.get(1).getStmt()).isEqualTo("op two");
    }

    /**
     * This test uses the compatible names for the sake of demonstration. Users should know that they can use op and
     * statement terms interchangeably in op templates.
     */
    @Test
    public void testMixedForms() {
        RawOpsLoader ysl = new RawOpsLoader();
        RawOpsDocList docs = ysl.loadString(
            "statement:\n" +
                " - name: s1\n" +
                "   stmt: statement one\n" +
                " - statement two\n" +
                " - s3: statement three\n" +
                " - ST4: statement four\n" +
                "   type: organic\n"
        );
        assertThat(docs.getOpsDocs()).hasSize(1);
        RawOpsDoc doc = docs.getOpsDocs().get(0);
        assertThat(doc.getRawOpDefs()).hasSize(4);
        List<RawOpDef> stmts = doc.getRawOpDefs();
        assertThat(stmts.get(0)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(0).getName()).isEqualTo("s1");
        assertThat(stmts.get(0).getStmt()).isEqualTo("statement one");
        assertThat(stmts.get(1)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(1).getName()).isEqualTo("stmt2");
        assertThat(stmts.get(1).getStmt()).isEqualTo("statement two");
        assertThat(stmts.get(2)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(2).getName()).isEqualTo("s3");
        assertThat(stmts.get(2).getStmt()).isEqualTo("statement three");
        assertThat(stmts.get(3)).isOfAnyClassIn(RawOpDef.class);
        assertThat(stmts.get(3).getName()).isEqualTo("ST4");
        assertThat(stmts.get(3).getStmt()).isEqualTo("statement four");
        assertThat(stmts.get(3).getParams().get("type")).isEqualTo("organic");

    }
}
