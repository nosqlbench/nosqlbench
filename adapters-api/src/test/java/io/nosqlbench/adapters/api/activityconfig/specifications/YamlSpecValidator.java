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

package io.nosqlbench.adapters.api.activityconfig.specifications;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawYamlLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.nb.spectest.api.STAssemblyValidator;
import io.nosqlbench.nb.spectest.core.STNodeAssembly;
import io.nosqlbench.nb.spectest.loaders.STDefaultLoader;
import io.nosqlbench.nb.spectest.testmodels.STNamedCodeTuples;
import io.nosqlbench.nb.spectest.testmodels.STNodeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <P>This validator looks at a {@link STNodeAssembly} as a sequence of
 * 6 parsed nodes, interpreted as 3 2-tuples of name and content,
 * where the three tuples represent:
 * <OL>
 *     <LI>An example op template in YAML form as provided by a user</LI>
 *     <LI>An equivalent example op template in JSON form as provided by a user</LI>
 *     <LI>The normalized op template datastructure as provided to the driver developer,
 *     represented as a JSON schematic.</LI>
 * </OL>
 *
 * <P>These are checked for validity by first checking the first and second for data structure
 * equivalence, and then processing the first through the op template API and checking the
 * result with the third part.</P>
 *
 * <P>This validator is meant be used with {@link STNodeAssembly}s which are found by the
 * {@link STDefaultLoader} scanner type.</P>
 *
 *
 * </P>
 */
public class YamlSpecValidator implements STAssemblyValidator {
    private final static Logger logger = LogManager.getLogger(YamlSpecValidator.class);
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void validate(STNodeAssembly assembly) {
        runTest(assembly);
    }

    private void runTest(STNodeAssembly testblock) {

        if (testblock.size() == 6) {
            STNamedCodeTuples tuples = testblock.getAsNameAndCodeTuples();
            String name = tuples.getTypeSignature();
            System.out.println(testblock.get(0).getDesc());
            System.out.println(testblock.get(0).getLocationRef());
            testBracket_YamlJsonOps(tuples);
        } else {
            throw new RuntimeException("Test block sized " + testblock.size() + " unrecognized by test loader.");
        }

    }

    private void testBracket_YamlJsonOps(STNamedCodeTuples tuples) {

        validateYamlWithJson(
            tuples.get(0).getName(),
            tuples.get(0).getData(),
            tuples.get(1).getData(),
            tuples.get(1),
            false);

        validateYamlWithOpsModel(
            tuples.get(0).getName(),
            tuples.get(0).getData(),
            tuples.get(2).getData(),
            tuples.get(2)
        );
    }

    private void validateYamlWithOpsModel(String desc, String yaml, String json, STNodeReference testref) {
        System.out.format("%-40s", "- checking yaml->ops");

        try {
            JsonElement elem = JsonParser.parseString(json);
            if (elem.isJsonArray()) {
                Type type = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> expectedList = gson.fromJson(json, type);

                OpsDocList stmtsDocs = OpsLoader.loadString(yaml, OpTemplateFormat.yaml, new HashMap<>(), null);
                List<OpTemplate> stmts = stmtsDocs.getOps();
                List<Map<String, Object>> stmt_objs = stmts.stream().map(OpTemplate::asData).collect(Collectors.toList());

                try {
                    Assertions.assertThat(stmt_objs).isEqualTo(expectedList);
                } catch (Throwable t) {
                    System.out.println("JSON version:\n"+gson.toJson(stmt_objs)+"\n");
                    throw t;
                }
            } else {
                throw new RuntimeException("The structurally normalized op template must be a list.");
            }

            System.out.println("OK");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * Compare one or more raw yaml docs to JSON5 representation of the same.
     * For clarity in the docs, a single object is allowed in the json5, in which case
     * an error is thrown if the yaml side contains more or less than 1 element.
     *  @param desc A moniker describing the test
     * @param yaml YAML describing a templated workload
     * @param json JSON describing a templated workload
     * @param debug
     */
    private void validateYamlWithJson(String desc, String yaml, String json, STNodeReference testset, boolean debug) {
        System.out.format("%-40s", "- checking yaml->json");

        try {
            List<Map<String, Object>> docmaps = new RawYamlLoader().loadString(logger, yaml);
            JsonElement elem = null;
            try {
                elem = JsonParser.parseString(json);
            } catch (JsonSyntaxException jse) {
                throw new RuntimeException("Not recongized as JSON:\n" + json);
            }
            if (elem.isJsonArray()) {
                Type type = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> expectedList = gson.fromJson(json, type);
                Assertions.assertThat(docmaps).isEqualTo(expectedList);
                System.out.println("OK");
            } else if (elem.isJsonObject()) {
                Map<String, Object> expectedSingle = gson.fromJson(json, Map.class);

                compareEach(expectedSingle, docmaps.get(0));
                Assertions.assertThat(docmaps.get(0)).isEqualTo(expectedSingle);
                if (docmaps.size() != 1) {
                    throw new RuntimeException("comparator expected a single object, but found " + docmaps.size());
                }
                System.out.println("OK");
            } else if (elem.isJsonPrimitive()) {
                throw new RuntimeException("Invalid type: JsonPrimitive types are not compatible with this validator. Objects and Arrays are.");
            } else {
                System.out.println("ERROR");
                throw new RuntimeException("unknown type in comparator: " + json);
            }
        } catch (Exception e) {
            String sb = "error while verifying model:\n" +
                " path: " + testset.getPath().toString() + "\n" +
                " line: " + testset.getLineNumber() + "\n";

            logger.error(sb + ": " + e.getMessage(), e);
            throw new RuntimeException(e);
        }


    }

    private void compareEach(List<Map<String, Object>> expected, List<Map<String, Object>> docmaps) {
        Assertions.assertThat(docmaps).isEqualTo(expected);
    }

    private void compareEach(Map<String, Object> structure, Map<String, Object> stringObjectMap) {
        Assertions.assertThat(stringObjectMap).isEqualTo(structure);
    }

}
