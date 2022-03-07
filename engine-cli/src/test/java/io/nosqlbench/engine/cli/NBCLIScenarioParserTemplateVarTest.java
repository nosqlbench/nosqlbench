package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIScenarioParserTemplateVarTest {

    @Test
    public void testMultipleOccurencesOfSameTemplateVar() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios-templatevars" });
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        StmtsDocList workload1 = StatementsLoader.loadPath(null, cmds.get(0).getArg("workload"),cmds.get(0).getParams());
        OpTemplate optpl1 = workload1.getStmts().get(0);
        System.out.println("op from cmd1:"+optpl1);
        assertThat(optpl1.getStmt()).contains("cycle {cycle} replaced replaced\n");

        StmtsDocList workload2 = StatementsLoader.loadPath(null, cmds.get(1).getArg("workload"),cmds.get(1).getParams());
        OpTemplate optpl2 = workload2.getStmts().get(0);
        System.out.println("op from cmd2:"+optpl2);
        assertThat(optpl2.getStmt()).contains("cycle {cycle} def1 def1\n");
    }

    @Test
    public void testThatCLIOverridesWorkForTemplateVars() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios-templatevars", "tvar1=overridden" });
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        StmtsDocList workload1 = StatementsLoader.loadPath(null, cmds.get(0).getArg("workload"),cmds.get(0).getParams());
        OpTemplate optpl1 = workload1.getStmts().get(0);
        System.out.println("op from cmd1:"+optpl1);
        assertThat(optpl1.getStmt()).contains("cycle {cycle} overridden overridden\n");
    }

    @Test
    public void testThatAdditionalCLIParamIsAdded() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"local/example-scenarios-templatevars", "tvar3=tval3"});
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);
        assertThat(cmds).hasSize(2);
        assertThat(cmds.get(0).getParams().get("tvar3")).isEqualTo("tval3");
        assertThat(cmds.get(1).getParams().get("tvar3")).isEqualTo("tval3");
    }

}
