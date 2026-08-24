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
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorReader;
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

}
