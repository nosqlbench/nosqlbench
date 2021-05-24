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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UniformYamlRawReaderTest {

    private final static Logger logger = LogManager.getLogger(UniformYamlRawReaderTest.class);

    private static final Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String summarize(Node node) {
        StringBuilder sb = new StringBuilder();
        while (node!=null) {
            sb.append("-> ").append(node.getClass().getSimpleName()).append("\n");
            sb.append(node.getChars()).append("\n");
            node=node.getNext();
        }
        return sb.toString();
    }

    public String summarize(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            sb.append(node.getClass().getSimpleName()).append("\n");
            sb.append(node.getChars()).append("\n");
        }
        return sb.toString();
    }

    @Test
    public void testAllForms() {

        LinkedList<TestSet> tests = new LinkedList<>();

        List<Content<?>> yaml = NBIO.fs().prefix("target/classes/workload_definition/").name("templated_workloads").extension("md").list();

        Pattern emphasis = Pattern.compile("\\*(.*?)\\*\n");
        Class<?> fcbclass = FencedCodeBlock.class;
        NodePredicates p = new NodePredicates(emphasis,fcbclass,emphasis,fcbclass,emphasis,fcbclass);

        List<TestBlock> testblocks = new ArrayList<>();

        for (Content<?> content : yaml) {
            Document parsed = parser.parse(content.asString());
            Node node = parsed.getFirstChild();
//            summarize(node,System.out);
            String heading = "none";
            int index = 0;

            while (node != null) {
                if (node instanceof Heading) {
                    heading = node.getChars().toString();
                    node = node.getNext();
                    index=0;
                }

                if (p.test(node)) {
                    List<Node> found = p.get();
//                    System.out.println(summarize(found));
                    String label = heading + String.format("-%02d", (++index));
                    testblocks.add(new TestBlock(
                        new TestSet(label,found.get(0),found.get(1),content.asPath()),
                        new TestSet(label,found.get(2),found.get(3),content.asPath()),
                        new TestSet(label,found.get(4),found.get(5),content.asPath())
                    ));
                    node=found.get(found.size()-1);
                }
                if (node!=null) {
                    node = node.getNext();
                }
            }

            for (TestBlock testblock : testblocks) {
                runTest(testblock);
            }
//
//            while (node.getNext())
//
//            Node node = parsed.getFirstChildAny(FencedCodeBlock.class, Heading.class);
//
//            String heading = "none";
//            int index = 0;
//            while (node != null) {
//                if (node instanceof Heading) {
//                    heading = node.getChars().toString();
//                    node = node.getNextAny(FencedCodeBlock.class, Heading.class);
//                    index=0;
//                } else if (node instanceof FencedCodeBlock) {
//                    tests.add(new TestSet(heading+String.format("-%02d",(++index+1)/2), (FencedCodeBlock)node));
//                    node = node.getNextAny(FencedCodeBlock.class, Heading.class);
//                }
//            }
        }

    }

    private void runTest(TestBlock testblock) {

        if (testblock.size()==3) {
            String tuple = testblock.get(0).info.toString() + "->" + testblock.get(1).info + "->" + testblock.get(2).info;
            tuple = tuple.replaceAll("[^-a-zA-Z0-9<> _]","");

            System.out.println(testblock.get(0).getDesc());

            if (tuple.equals("yaml->json->ops")) {
                testYamlJsonOps(testblock);
            }
        } else {
            throw new RuntimeException("Test block sized " + testblock.size() + " unrecognized by test loader.");
        }

    }

    /**
     * Not thread-safe!
     */
    private static class NodePredicates implements Predicate<Node>,Supplier<List<Node>> {
        final List<Predicate<Node>> predicates;
        final List<Node> found = new ArrayList<>();

        public NodePredicates(Object... predicates) {
            this.predicates = Arrays.stream(predicates).map(NodePredicate::new).collect(Collectors.toList());
        }

        @Override
        public boolean test(Node node) {
            found.clear();

            for (Predicate<Node> predicate : predicates) {
                if (node == null) {
                    return false;
                }
                if (!predicate.test(node)) {
                    return false;
                }
                found.add(node);

                node = node.getNext();
            }
            return true;
        }

        @Override
        public List<Node> get() {
            return List.copyOf(found);
        }
    }

    /**
     * Not thread-safe!
     */
    private static class NodePredicate implements Predicate<Node>, Supplier<Node> {
        private final Predicate<Node> predicate;
        private Node found= null;
        public NodePredicate(Object o) {
            this.predicate = resolvePredicate(o);
        }

        private Predicate<Node> resolvePredicate(Object object) {
            if (object instanceof Predicate) {
                return (Predicate<Node>) object;
            } else if (object instanceof Class) {
                return (n) -> object.equals(n.getClass());
            } else if (object instanceof Pattern) {
                return (n) -> ((Pattern) object).matcher(n.getChars()).matches();
            } else if (object instanceof CharSequence) {
                return (n) -> Pattern.compile(object.toString()).matcher(n.getChars()).matches();
            } else {
                throw new RuntimeException("no Node predicate for type " + object.getClass().getSimpleName());
            }
        }

        @Override
        public boolean test(Node node) {
            this.found = null;
            boolean isFound = predicate.test(node);
            if (isFound)
            {
                this.found = node;
            }
            return isFound;
        }

        @Override
        public Node get() {
            return this.found;
        }
    }

    private final static Predicate<Node> FORMATS = new Predicate<Node>() {

        @Override
        public boolean test(Node node) {
            int lookahead = 6;
            List<Class<?>> types = new ArrayList<>(lookahead);

            return false;
        }
    };

    private void testYamlJsonOps(TestBlock block) {
        validateYamlWithJson(block.get(0).getDesc(), block.get(0).text.toString(), block.get(1).text.toString());
        validateYamlWithOpsModel(block.get(0).getDesc(), block.get(0).text.toString(), block.get(2).text.toString());
    }

    private void validateYamlWithOpsModel(String desc, String yaml, String json) {
        System.out.format("%-40s","- checking yaml->ops");

        JsonParser parser = new JsonParser();
        try {
            JsonElement elem = parser.parse(json);
            if (elem.isJsonArray()) {
                Type type = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> expectedList = gson.fromJson(json, type);
                StmtsDocList stmtsDocs = StatementsLoader.loadString(yaml);
                List<OpTemplate> stmts = stmtsDocs.getStmts();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String optemplate_as_json = gson.toJson(stmts);
                System.out.print(optemplate_as_json);

            }

            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("ERROR");
        }



    }


    /**
     * Compare one or more raw yaml docs to JSON5 representation of the same.
     * For clarity in the docs, a single object is allowed in the json5, in which case
     * an error is thrown if the yaml side contains more or less than 1 element.
     * @param desc A moniker describing the test
     * @param yaml YAML describing a templated workload
     * @param json JSON describing a templated workload
     */
    private void validateYamlWithJson(String desc, String yaml, String json) {
        System.out.format("%-40s","- checking yaml->json");

//        StmtsDocList stmts = StatementsLoader.loadString(yaml);
        JsonParser parser = new JsonParser();

        try {
            List<Map<String, Object>> docmaps = new RawYamlLoader().loadString(logger, yaml);
            JsonElement elem = parser.parse(json);
            if (elem.isJsonArray()) {
                Type type = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<Map<String, Object>> expectedList = gson.fromJson(json, type);
                assertThat(docmaps).isEqualTo(expectedList);
                System.out.println("OK");
            } else if (elem.isJsonObject()) {
                Map<String, Object> expectedSingle = gson.fromJson(json, Map.class);
                compareEach(expectedSingle, docmaps.get(0));
                assertThat(docmaps.get(0)).isEqualTo(expectedSingle);
                if (docmaps.size()!=1) {
                    throw new RuntimeException("comparator expected a single object, but found " + docmaps.size());
                }
                System.out.println("OK");
            } else {
                System.out.println("ERROR");
                throw new RuntimeException("unknown type in comparator: " + json);
            }
        } catch (Exception e) {
            logger.error("Error while processing data:\n" + json + ": " + e.getMessage(), e);
            throw new RuntimeException(e);
        }


    }

    private void compareEach(List<Map<String, Object>> expected, List<Map<String, Object>> docmaps) {
        assertThat(docmaps).isEqualTo(expected);
    }

    private void compareEach(Map<String, Object> structure, Map<String, Object> stringObjectMap) {
        assertThat(stringObjectMap).isEqualTo(structure);
    }

}
