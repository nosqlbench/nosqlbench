package io.nosqlbench.exprs.lib.vectors;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.expr.ExprFunctionProvider;
import io.nosqlbench.nb.api.expr.annotations.ExprExample;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;
import io.nosqlbench.vectordata.Catalog;
import io.nosqlbench.vectordata.CatalogSources;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.PrefetchPlan;
import io.nosqlbench.vectordata.PrefetchReport;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.VvecReader;
import io.nosqlbench.vectordata.WholeFacetFallback;
import io.nosqlbench.virtdata.lib.vectors.vectordata.WindowedReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = ExprFunctionProvider.class, selector = "virtdata")
public class VectorDataExprs implements ExprFunctionProvider {
    private final static Logger logger = LogManager.getLogger(VectorDataExprs.class);

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "dataset",
        synopsis = "dataset(\"dataset:profile\")",
        description = "Return the TestDataView for the named dataset profile."
    )
    public TestDataView dataset(String datasetNameAndProfile) {
        if (!datasetNameAndProfile.contains(":")) {
            logger.warn("datasetNameAndProfile missing profile:" + datasetNameAndProfile);
        }
        return Catalog.of(CatalogSources.defaults()).openProfile(datasetNameAndProfile);
    }
    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "baseVectors",
        synopsis = "baseVectors(\"dataset:profile\")",
        description = "Return the BaseVectors associated with the dataset profile."
    )
    public VectorReader<float[]> baseVectors(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).baseVectors();
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "queryVectors",
        synopsis = "queryVectors(\"dataset:profile\")",
        description = "Return the QueryVectors associated with the dataset profile."
    )
    public VectorReader<float[]> queryVectors(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).queryVectors();
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "neighborDistances",
        synopsis = "neighborDistances(\"dataset:profile\")",
        description = "Return the NeighborDistances associated with the dataset profile."
    )
    public VectorReader<float[]> neighborDistances(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).neighborDistances();
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "neighborIndices",
        synopsis = "neighborIndices(\"dataset:profile\")",
        description = "Return the NeighborIndices associated with the dataset profile."
    )
    public VectorReader<int[]> neighborIndices(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).neighborIndices();
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "metadataResults",
        synopsis = "metadataResults(\"dataset:profile\")",
        description = "Return the MetadataResults associated with the dataset profile."
    )
    public VectorReader<int[]> metadataResults(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).metadataResults();
    }

    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"metadata_results\""}, expectNotNull = true)
    @ExprFunctionSpec(
        name = "facet",
        synopsis = "facet(\"dataset:profile\", \"facet_name\")",
        description = "Return a reader for any named facet of the dataset profile, by canonical name or alias."
    )
    public VectorReader<?> facet(String datasetNameAndProfile, String facetName) {
        return dataset(datasetNameAndProfile).openFacet(facetName);
    }

    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"[0..100)\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"100\""}, expectNotNull = true)
    @ExprFunctionSpec(
        name = "windowedFacet",
        synopsis = "windowedFacet(\"dataset:profile\", \"facet_name\", \"window\")",
        description = "Return a reader for a named facet, clipped to a record window in the reader's own coordinates."
    )
    public VectorReader<?> windowedFacet(String datasetNameAndProfile, String facetName, String window) {
        return WindowedReader.clip(dataset(datasetNameAndProfile).openFacet(facetName), window);
    }

    @ExprExample(args = {"\"airports:demo\"", "\"metadata_content\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"metadata_content\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "variableFacet",
        synopsis = "variableFacet(\"dataset:profile\", \"facet_name\")",
        description = "Return a variable-dimension (vvec) reader for a named facet of the dataset profile."
    )
    public VvecReader<?> variableFacet(String datasetNameAndProfile, String facetName) {
        return dataset(datasetNameAndProfile).openVariableFacet(facetName);
    }

    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"[0..100)\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"\""}, expectNotNull = true)
    @ExprFunctionSpec(
        name = "prefetchPlan",
        synopsis = "prefetchPlan(\"dataset:profile\", \"facet_name\", \"window\")",
        description = "Report what prefetching a record window of a facet would fetch, without fetching any of it. An empty window means the whole facet."
    )
    public PrefetchPlan prefetchPlan(String datasetNameAndProfile, String facetName, String window) {
        return dataset(datasetNameAndProfile).prefetchPlan(facetName, DSWindow.parse(window));
    }

    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"[0..100)\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"\""}, expectNotNull = true)
    @ExprFunctionSpec(
        name = "prefetch",
        synopsis = "prefetch(\"dataset:profile\", \"facet_name\", \"window\")",
        description = "Fetch a record window of a facet and return when it is resident. An empty window requests the whole facet; an unresolvable window is refused rather than fetching everything."
    )
    public PrefetchReport prefetch(String datasetNameAndProfile, String facetName, String window) {
        return dataset(datasetNameAndProfile).prefetch(facetName, DSWindow.parse(window), WholeFacetFallback.REFUSE);
    }

    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"[0..100)\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\"", "\"base_vectors\"", "\"\""}, expectNotNull = true)
    @ExprFunctionSpec(
        name = "prefetchBackground",
        synopsis = "prefetchBackground(\"dataset:profile\", \"facet_name\", \"window\")",
        description = "Start fetching a record window of a facet on another thread and return the handle; join() waits, cancel() stops between ranges."
    )
    public PrefetchHandle prefetchBackground(String datasetNameAndProfile, String facetName, String window) {
        return dataset(datasetNameAndProfile).prefetchInBackground(facetName, DSWindow.parse(window), WholeFacetFallback.REFUSE);
    }

}
