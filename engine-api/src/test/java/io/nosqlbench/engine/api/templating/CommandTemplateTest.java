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

package io.nosqlbench.engine.api.templating;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.templating.CommandTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTemplateTest {
    private final static Logger logger = LogManager.getLogger(CommandTemplateTest.class);

    @Test
    public void testCommandTemplate() {
        OpsDocList opsDocs = OpsLoader.loadString("ops:\n" +
                " - s1: test1=foo test2=bar",
            OpTemplateFormat.yaml, Map.of(), null);
        OpTemplate optpl = opsDocs.getOps().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        assertThat(ct.isStatic()).isTrue();
    }

    @Test
    public void testCommandTemplateFormat() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        OpsDocList stmtsDocs = OpsLoader.loadString("ops:\n" +
            " - s1: test1=foo test2={bar}\n" +
            "   bindings:\n" +
            "    bar: NumberNameToString();\n",
            OpTemplateFormat.yaml, Map.of(), null
        );
        OpTemplate optpl = stmtsDocs.getOps().get(0);
        CommandTemplate ct = new CommandTemplate(optpl);
        String format = gson.toJson(ct);
        logger.debug(format);

    }

}
