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

import io.nosqlbench.nb.spectest.loaders.STDefaultLoader;
import io.nosqlbench.nb.spectest.loaders.STNodePredicate;
import io.nosqlbench.nb.spectest.types.STAssemblyValidator;
import io.nosqlbench.nb.spectest.types.STBuilderFacets;
import io.nosqlbench.nb.spectest.types.STPathLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SpecTestBuilder implements STBuilderFacets.All {

    protected List<Path> paths = new ArrayList<>();
    protected List<STPathLoader> scanners  = new ArrayList<>();
    protected List<STAssemblyValidator> validators  = new ArrayList<>();

    @Override
    public STBuilderFacets.WantsPathsOrScannersOrValidators paths(Path... paths) {
        this.paths.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public STBuilderFacets.WantsValidatorsOrDone scanners(STPathLoader... scanners) {
        this.scanners.addAll(Arrays.asList(scanners));
        return this;
    }

    @Override
    public STBuilderFacets.WantsValidatorsOrDone scanners(STNodePredicate predicate) {
        return this.matchNodes(predicate);
    }

    @Override
    public STBuilderFacets.WantsValidatorsOrDone matchNodes(Object... predicates) {
        return this.scanners(new STDefaultLoader(predicates));
    }

    @Override
    public STBuilderFacets.Done validators(STAssemblyValidator... validators) {
        this.validators.addAll(Arrays.asList(validators));
        return this;
    }

}
