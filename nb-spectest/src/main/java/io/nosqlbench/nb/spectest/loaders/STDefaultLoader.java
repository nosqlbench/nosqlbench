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
import io.nosqlbench.nb.spectest.core.STNodeAssembly;
import io.nosqlbench.nb.spectest.types.STAssemblyValidator;
import io.nosqlbench.nb.spectest.types.STNodeLoader;
import io.nosqlbench.nb.spectest.types.STPathLoader;

import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <P>This scanner looks for testable specifications which are matched as a sequence of
 * 3 2-tuples, described in {@link YamlSpecValidator}. The required markdown structure for
 * this looks like:
 * <PRE>{@code
 * ## call form with defaults
 *
 * *yaml:*
 * ```yaml
 * name: TEMPLATE(myname,thedefault)
 * ```
 *
 * *json:*
 * ```json5
 * {
 *     "name": "thedefault"
 * }
 * ```
 *
 * *ops:*
 * ```json5
 * []
 * ```
 * }</PRE>
 * </P>
 *
 * <P>Specifically, the emphasis and colon paragraph blocks indicate the naming of the elements, and the
 * fenced code sections represent the content. The name elements are not matched for the name specifically,
 * although the {@link STAssemblyValidator} which consumes these {@link STNodeAssembly}s will interpret them
 * as described above.
 * </P>
 *
 */
public class STDefaultLoader implements STPathLoader {
    private final STNodePredicates predicates;
    private static final Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();

    public STDefaultLoader(Object... predicates) {

        if (predicates.length==0) {
            throw new InvalidParameterException("An empty spec scanner is invalid.");
        }
        if ((predicates.length % 2) != 0) {
            throw new InvalidParameterException("You can only provide predicates in sequences of 2-tuples, where" +
                "each even index is a naming element and each odd index is the associated test content. " +
                "But " + predicates.length + " were provided: " + Arrays.toString(predicates));
        }
        this.predicates = new STNodePredicates(predicates);
    }

    @Override
    public List<STNodeAssembly> apply(Path specPath) {
        List<STNodeAssembly> assemblies = new ArrayList<>();
        List<Path> matchingPaths = STFileScanner.findMatching(specPath);
        STNodeLoader nodeLoader = new STDefaultNodeLoader(predicates);

        for (Path matchingPath : matchingPaths) {
            List<STNodeAssembly> found = nodeLoader.apply(matchingPath, null);
            assemblies.addAll(found);
        }


        return assemblies;
    }
}
