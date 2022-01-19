package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIScenarioParserTemplateVarTest {

    @Test
    public void providePathForScenario() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios-templatevars" });
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        StmtsDocList workload1 = StatementsLoader.loadPath(null, cmds.get(0).getArg("workload"),cmds.get(0).getParams());
        OpTemplate optpl = workload1.getStmts().get(0);
        assertThat(optpl.getStmt()).contains("cycle {cycle} replaced replaced\n");
        System.out.println("op:"+optpl);

    }

}
