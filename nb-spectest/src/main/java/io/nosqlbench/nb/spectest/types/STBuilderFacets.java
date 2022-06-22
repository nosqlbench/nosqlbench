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

package io.nosqlbench.nb.spectest.types;

import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.nb.spectest.core.SpecTest;
import io.nosqlbench.nb.spectest.core.SpecTestBuilder;
import io.nosqlbench.nb.spectest.loaders.STNodePredicate;
import io.nosqlbench.nb.spectest.loaders.STNodePredicates;

import java.nio.file.Path;

public interface STBuilderFacets {

    interface All extends WantsPaths, WantsPathsOrScannersOrValidators, WantsScannersOrValidators {}

    interface WantsPaths {
        /**
         * Provide additional path or paths, which can be either a readable file or a directory containing readable
         * files or other directories. Only files which match a markdown path with a '.md' extension
         * will be validated.
         * @param paths Paths to test
         * @return this {@link SpecTestBuilder} for method chaining
         */
        WantsPathsOrScannersOrValidators paths(Path... paths);
        /**
         * Provide additional path or paths, which can be either a readable file or a directory containing readable
         * files or other directories. Only files which match a markdown path with a '.md' extension
         * will be validated.
         * @param paths Paths to test
         * @return this {@link SpecTestBuilder} for method chaining
         */
        default WantsPathsOrScannersOrValidators paths(String... paths) {
            Path[] args = new Path[paths.length];
            for (int i = 0; i < paths.length; i++) {
                args[i]=Path.of(paths[i]);
            }
            return this.paths(args);
        }
        /**
         * Provide an additional path, which can be either a readable file or a directory containing readable
         * files or other directories. Only files which match a markdown path with a '.md' extension
         * will be validated.
         * @param path Paths to test
         * @return this {@link SpecTestBuilder} for method chaining
         */
        default WantsPathsOrScannersOrValidators path(Path path) {
            return this.paths(path);
        }

        /**
         * Provide an additional path or paths, which can be either a readable file or a directory containing readable
         * files or other directories. Only files which match a markdown path with a '.md' extension
         * will be validated.
         * @param path Paths to test
         * @return this {@link SpecTestBuilder} for method chaining
         */
        default WantsPathsOrScannersOrValidators path(String path) {
            return this.paths(Path.of(path));
        }
    }

    interface WantsPathsOrScannersOrValidators extends WantsPaths, WantsScannersOrValidators {
    }

    interface WantsScannersOrValidators extends WantsValidatorsOrDone {
        /**
         * Attach a set of scanners against {@link Node} sequences
         * @param scanners
         * @return
         */
        WantsValidatorsOrDone scanners(STPathLoader... scanners);
        WantsValidatorsOrDone scanners(STNodePredicate predicate);

        /**
         * <P>Attach a {@link Node} scanner to this spec test builder by describing the
         * sequence of nodes which are valid, with no extra node types allowed between the
         * specified elements. This is a direct sequence matcher that is checked at each
         * position in the parsed element stream. When a sequence is found, the search
         * is resumed on the next element not included in the result.</P>
         *
         * <P>The predicates can be one of the types supported by {@link STNodePredicate}
         * and {@link STNodePredicates}.</P>
         *
         * @param predicates The pattern to match
         * @return this SpecTestBuilder for builder method chaining
         */
        WantsValidatorsOrDone matchNodes(Object... predicates);
    }

    interface WantsValidatorsOrDone extends Done{
        Done validators(STAssemblyValidator... validators);
    }

    interface Done {
        SpecTest build();
    }
}
