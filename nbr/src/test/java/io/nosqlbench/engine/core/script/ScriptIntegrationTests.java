/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.core.script;

import io.nosqlbench.engine.core.lifecycle.ScenarioResult;
import io.nosqlbench.engine.core.lifecycle.ScenariosResults;
import io.nosqlbench.nb.annotations.Maturity;
import org.apache.commons.compress.utils.IOUtils;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptIntegrationTests {

    public static ScenarioResult runScenario(String scriptname, String... params) {
        if ((params.length % 2) != 0) {
            throw new RuntimeException("params must be pairwise key, value, ...");
        }
        Map<String, String> paramsMap = new HashMap<>();

        for (int i = 0; i < params.length; i += 2) {
            paramsMap.put(params[i], params[i + 1]);
        }
        String scenarioName = "scenario " + scriptname;
        System.out.println("=".repeat(29) + " Running SYNC integration test for: " + scenarioName);
        ScenariosExecutor e = new ScenariosExecutor(ScriptIntegrationTests.class.getSimpleName() + ":" + scriptname, 1);
        Scenario s = new Scenario(scenarioName, Scenario.Engine.Graalvm,"stdout:300", Maturity.Any);
        s.addScenarioScriptParams(paramsMap);
        ClassLoader cl = AsyncScriptIntegrationTests.class.getClassLoader();
        String script;
        try {
            InputStream sstream = cl.getResourceAsStream("scripts/sync/" + scriptname + ".js");
            byte[] bytes = IOUtils.toByteArray(sstream);
            script = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        s.addScriptText(script);
//        s.addScriptText("load('classpath:scripts/sync/" + scriptname + ".js');");
        e.execute(s);
        ScenariosResults scenariosResults = e.awaitAllResults();
        ScenarioResult scenarioResult = scenariosResults.getOne();
        return scenarioResult;
    }


    @BeforeAll
    public static void logit() {
        System.out.println("Running SYNC version of Script Integration Tests.");
    }


    @Test
    public void testCycleRate() {

        ScenarioResult scenarioResult = runScenario("sync_cycle_rate");
        String iolog = scenarioResult.getIOLog();
        System.out.println("iolog\n" + iolog);
        Pattern p = Pattern.compile(".*mean cycle rate = (\\d[.\\d]+).*", Pattern.DOTALL);
        Matcher m = p.matcher(iolog);
        assertThat(m.matches()).isTrue();

        String digits = m.group(1);
        assertThat(digits).isNotEmpty();
        double rate = Double.valueOf(digits);
        assertThat(rate).isCloseTo(1000, Offset.offset(100.0));
    }

}
