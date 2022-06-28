/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.spectest.loaders;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.nb.spectest.api.STNodeLoader;
import io.nosqlbench.nb.spectest.core.STDebug;
import io.nosqlbench.nb.spectest.core.STNode;
import io.nosqlbench.nb.spectest.core.STNodeAssembly;
import io.nosqlbench.nb.spectest.traversal.STNodePredicates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class STDefaultNodeLoader implements STNodeLoader, STDebug {

    private final STNodePredicates predicates;
    private final Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
    private boolean debug;


    public STDefaultNodeLoader(Object... predicates) {
//        if (predicates.length==0) {
//            throw new InvalidParameterException("An empty spec scanner is invalid.");
//        }
//        if ((predicates.length % 2) != 0) {
//            throw new InvalidParameterException("You can only provide predicates in sequences of 2-tuples, where" +
//                "each even index is a naming element and each odd index is the associated test content. " +
//                "But " + predicates.length + " were provided: " + Arrays.toString(predicates));
//        }
        this.predicates = new STNodePredicates(predicates);

    }

    @Override
    public List<STNodeAssembly> apply(Path path, Node node) {
        List<STNodeAssembly> assemblies = new ArrayList<>();

        if (node == null) {
            if (path == null) {
                throw new InvalidParameterException("You must provide at least a path.");
            }
            try {
                String input = Files.readString(path);
                node = parser.parse(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (node instanceof Document d) {
            node = d.getFirstChild();
        }

        STHeadingScanner headings = new STHeadingScanner(" > ");
        headings.update(path); // null is allowed here

        while (node != null) {
            headings.update(node);
            Optional<List<Node>> optionalStanza = predicates.apply(node);

            if (optionalStanza.isPresent()) {
                List<Node> found = optionalStanza.get();

                List<STNode> stnodes = found.stream()
                    .map(
                        n -> new STNode(headings, n, path)
                    )
                    .toList();

                STNodeAssembly testassy = new STNodeAssembly(
                    stnodes.toArray(
                        new STNode[0]
                    )
                );

                node = found.get(found.size() - 1);
                headings.index();
                assemblies.add(testassy);
                if (debug) {
                    summarize(testassy);
                }
            }
            if (node != null) {
                node = node.getNext();
            }
        }

        return assemblies;
    }

    private void summarize(STNodeAssembly testassy) {
        for (STNode stNode : testassy) {
            String nodeClass = stNode.getRefNode().getClass().getSimpleName();
            String text = stNode.getRefNode().getChars().toString();

            String[] lines = text.split("\n");
            String header =lines[0].substring(0,Math.min(lines[0].length(),39));
            if (lines[0].length()>39) {
                header=header+"...";
            }
            if (!header.endsWith("\n")) {
                header = header+"\n";
            }
            System.out.format("%-20s|%-40s|(%-3d lines)\n",nodeClass,header.replaceAll("\n","\\n"),lines.length);
        }
    }

    @Override
    public void applyDebugging(boolean enabled) {
        this.debug = enabled;
    }
}
