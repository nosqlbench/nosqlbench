/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadyHttpOpTest {

    @Test
    public void testOnelineSpec() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: method=get uri=http://localhost/\n",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        ReadyHttpOp readyReq = new ReadyHttpOp(stmtDef);
        HttpOp staticReq = readyReq.apply(3);
    }

    @Test
    public void testRFCFormMinimal() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: get http://localhost/",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        ReadyHttpOp readyReq = new ReadyHttpOp(stmtDef);
        HttpOp staticReq = readyReq.apply(3);
    }

    @Test
    public void testRFCFormVersioned() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: get http://localhost/ HTTP/1.1",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        ReadyHttpOp readyReq = new ReadyHttpOp(stmtDef);
        HttpOp staticReq = readyReq.apply(3);
    }

    @Test
    public void testRFCFormHeaders() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: |\n" +
                "    get http://localhost/\n" +
                "    Content-Type: application/json" +
                "",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        ReadyHttpOp readyReq = new ReadyHttpOp(stmtDef);
        HttpOp staticReq = readyReq.apply(3);
    }

    @Test
    public void testRFCFormBody() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: |\n" +
                "    get http://localhost/\n" +
                "    \n" +
                "    body1",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        ReadyHttpOp readyReq = new ReadyHttpOp(stmtDef);
        HttpOp staticReq = readyReq.apply(3);
    }

    @Test
    public void testRFCAllValuesTemplated() {

        // This can not be fully resolved in the unit testing context, but it could be
        // in the integrated testing context. It is sufficient to verify parsing here.
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: |\n" +
                "    {method} {scheme}://{host}/{path}?{query} {version}\n" +
                "    Header1: {header1val}\n" +
                "    \n" +
                "    {body}\n" +
                "\n" +
                "bindings: \n" +
                " method: StaticString('test')\n" +
                " scheme: StaticString('test')\n" +
                " host: StaticString('test')\n" +
                " path: StaticString('test')\n" +
                " query: StaticString('test')\n" +
                " version: StaticString('test')\n" +
                " header1val: StaticString('test')\n" +
                " body: StaticString('test')\n",
            Map.of()
        );
        OpTemplate stmtDef = docs.getStmts().get(0);

        Map<String, String> parse = HttpFormatParser.parseInline(stmtDef.getStmt().orElseThrow());
        assertThat(parse).containsAllEntriesOf(
                Map.of(
                        "method", "{method}",
                        "uri", "{scheme}://{host}/{path}?{query}",
                        "version", "{version}",
                        "Header1","{header1val}",
                        "body","{body}"
                )
        );


    }
}
