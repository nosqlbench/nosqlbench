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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCLIScenarioParserTemplateVarTest {

    @Test
    public void testMultipleOccurencesOfSameTemplateVar() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios-templatevars" });
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        OpsDocList workload1 = OpsLoader.loadPath(cmds.get(0).getArg("workload"),cmds.get(0).getParams());
        OpTemplate optpl1 = workload1.getOps().get(0);
        System.out.println("op from cmd1:"+optpl1);
        assertThat(optpl1.getStmt()).contains("cycle {cycle} replaced replaced\n");

        OpsDocList workload2 = OpsLoader.loadPath(cmds.get(1).getArg("workload"),cmds.get(1).getParams());
        OpTemplate optpl2 = workload2.getOps().get(0);
        System.out.println("op from cmd2:"+optpl2);
        assertThat(optpl2.getStmt()).contains("cycle {cycle} def1 def1\n");
    }

    @Test
    public void testThatCLIOverridesWorkForTemplateVars() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "local/example-scenarios-templatevars", "tvar1=overridden" });
        List<Cmd> cmds = opts.getCommands();
        cmds.forEach(System.out::println);

        OpsDocList workload1 = OpsLoader.loadPath(cmds.get(0).getArg("workload"),cmds.get(0).getParams());
        OpTemplate optpl1 = workload1.getOps().get(0);
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
