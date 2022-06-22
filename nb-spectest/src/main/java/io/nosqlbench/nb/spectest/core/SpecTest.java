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

package io.nosqlbench.nb.spectest.core;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import io.nosqlbench.nb.spectest.loaders.STFileScanner;
import io.nosqlbench.nb.spectest.types.STAssemblyValidator;
import io.nosqlbench.nb.spectest.types.STBuilderFacets;
import io.nosqlbench.nb.spectest.types.STPathLoader;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the entry point into a specification test. By default, all files
 * within the provided path are checked for the scanner patterns,
 * and all validators are run. This means:
 *
 * <OL>
 *     <LI>All provided paths which are markdown files will be scanned.</LI>
 *     <LI>All provided paths which are directories will be recursively scanned for contained markdown files.</LI>
 *     <LI>Every included markdown file will be scanned for matching element sequences.</LI>
 *     <LI>Every matching element sequence will be validated.</LI>
 * </OL>
 *
 * <p>The details of each of these steps should be documented in the specific classes which implement
 * the behavior. At a high level:
 *
 * Key concepts are:
 * <UL>
 *     <LI>Specification Files - These are the markdown files and included sets of specification tuples.
 *     Specification tuples are sequences of content elements which are recognizable by a scanner
 *     as going together to form a multi-part specification and test.</LI>
 *     <LI>Scanners - A scanner is a pattern matcher specification, in the form of a set of predicate objects.
 *     These are tested against sequences of elements that are found within specification files. Each
 *     time a scanner matches a sequence of elements, a specification template is extracted and submitted
 *     to a set of validators.</LI>
 *     <LI>Specification Template - This is merely a set of content elements which represents a specific
 *     specification or example. When a specification template is found, it is extracted with some context
 *     metadata that makes it easy to identify and report (like the heading), as well as the content needed
 *     for a specification validator to confirm whether or not it passes a test for validity.</LI>
 *     <LI>Validators - A validator knows how to assert the validity of a specification template. It is
 *     essentially a set of test assertions against a specification template. There are default validators
 *     which do different types of validation based on the content type.</LI>
 * </UL>
 *
 * Effectively, all specifications are described prosaically within the markdown, while the actual specifications
 * which are meant to be validated are captured within fenced code sections. The type of fenced code section
 * determines how the inner content is interpreted and validated.
 *
 * </p>
 */
public class SpecTest implements Runnable {

    private static final Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
    private final List<Path> paths;
    private final List<STPathLoader> pathLoaders;
    private final List<STAssemblyValidator> validators;

    private SpecTest(List<Path> paths, List<STPathLoader> pathLoaders, List<STAssemblyValidator> validators) {
        this.paths = paths;
        this.pathLoaders = pathLoaders;
        this.validators = validators;
    }

    @Override
    public void run() {
        Set<STNodeAssembly> testables = new LinkedHashSet<>();

        for (Path path : paths) {
            List<Path> matchingPaths = STFileScanner.findMatching(".*\\.md", paths.toArray(new Path[0]));
            for (Path matchingPath : matchingPaths) {
//                STNodeScanner scanner = new STDefaultNodeScanner();
                for (STPathLoader pathLoader : pathLoaders) {

                    List<STNodeAssembly> testableAssemblies = pathLoader.apply(matchingPath);
                    for (STNodeAssembly assembly : testableAssemblies) {
                        if (testables.contains(assembly)) {
                            throw new RuntimeException("Found duplicate testable assembly in path set:\n" +
                                "assembly: " + assembly.toString());
                        }
                        testables.add(assembly);
                    }
                }

            }
        }

        for (STNodeAssembly assembly : testables) {
            for (STAssemblyValidator validator : validators) {
                validator.validate(assembly);
            }
        }
    }

    public static STBuilderFacets.WantsPaths builder() {
        return new SpecTest.Builder();
    }

    private static class Builder extends STBuilder {
        @Override
        public SpecTest build() {
            return new SpecTest(paths,scanners,validators);
        }
    }
}
