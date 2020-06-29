package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTemplateTest {

    @Test
    public void testCommandTemplate() {
        StmtsDocList stmtsDocs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: test1=foo test2=bar");
        StmtDef stmtDef = stmtsDocs.getStmts().get(0);
        CommandTemplate ct = new CommandTemplate(stmtDef, false);
        assertThat(ct.isStatic()).isTrue();

    }

}