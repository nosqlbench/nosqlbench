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

package io.nosqlbench.engine.cli;

import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.cmdstream.Cmd;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIScenarioPreprocessorTemplateVarTest {

    @Test
    public void testMultipleOccurencesOfSameTemplateVar() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example_scenarios_templatevars" }, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        OpsDocList workload1 = OpsLoader.loadPath(cmds.get(0).getArgValue("workload"),cmds.get(0).getArgMap());
        OpTemplate optpl1 = workload1.getOps(true).get(0);
        System.out.println("op from cmd1:"+optpl1);
        assertThat(optpl1.getStmt()).contains("cycle {cycle} replaced replaced\n");

        OpsDocList workload2 = OpsLoader.loadPath(cmds.get(1).getArgValue("workload"),cmds.get(1).getArgMap());
        OpTemplate optpl2 = workload2.getOps(true).get(0);
        System.out.println("op from cmd2:"+optpl2);
        assertThat(optpl2.getStmt()).contains("cycle {cycle} def1 def1\n");
    }

    @Test
    public void testThatCLIOverridesWorkForTemplateVars() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example_scenarios_templatevars", "tvar1=overridden" }, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        OpsDocList workload1 = OpsLoader.loadPath(cmds.get(0).getArgValue("workload"),cmds.get(0).getArgMap());
        OpTemplate optpl1 = workload1.getOps(true).get(0);
        System.out.println("op from cmd1:"+optpl1);
        assertThat(optpl1.getStmt()).contains("cycle {cycle} overridden overridden\n");
    }

    @Test
    public void testThatAdditionalCLIParamIsAdded() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"local/example_scenarios_templatevars", "tvar3=tval3"}, NBCLIOptions.Mode.ParseAllOptions);
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);
        assertThat(cmds).hasSize(2);
        assertThat(cmds.get(0).getArgValueOrNull("tvar3")).isEqualTo("tval3");
        assertThat(cmds.get(1).getArgValueOrNull("tvar3")).isEqualTo("tval3");
    }

}
