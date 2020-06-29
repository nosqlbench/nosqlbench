package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.Test;

import java.net.http.HttpRequest;

public class ReadyHttpRequestTest {

    @Test
    public void testStaticTemplate() {
        StmtsDocList docs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: method=get\n");
        StmtDef stmtDef = docs.getStmts().get(0);

        ReadyHttpRequest readyReq = new ReadyHttpRequest(stmtDef);
        HttpRequest staticReq = readyReq.apply(3);
    }

}