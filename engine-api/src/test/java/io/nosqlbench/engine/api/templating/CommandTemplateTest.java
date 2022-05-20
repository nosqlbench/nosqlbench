package io.nosqlbench.engine.api.templating;

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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTemplateTest {

    @Test
    public void testCommandTemplate() {
        StmtsDocList stmtsDocs = StatementsLoader.loadString("" +
                "statements:\n" +
                " - s1: test1=foo test2=bar", Map.of());
        OpTemplate optpl = stmtsDocs.getStmts().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        assertThat(ct.isStatic()).isTrue();
    }

    @Test
    public void testCommandTemplateFormat() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StmtsDocList stmtsDocs = StatementsLoader.loadString("" +
            "statements:\n" +
            " - s1: test1=foo test2={bar}\n" +
            "   bindings:\n" +
            "    bar: NumberNameToString();\n",
            Map.of());
        OpTemplate optpl = stmtsDocs.getStmts().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        String format = gson.toJson(ct);
        System.out.println(format);

    }

}
