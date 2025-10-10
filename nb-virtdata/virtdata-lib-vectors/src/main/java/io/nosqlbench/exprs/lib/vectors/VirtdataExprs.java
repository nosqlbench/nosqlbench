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
import io.nosqlbench.vectordata.VectorTestData;
import io.nosqlbench.vectordata.discovery.TestDataView;
import io.nosqlbench.vectordata.spec.datasets.types.*;

@Service(value = ExprFunctionProvider.class, selector = "virtdata")
public class VirtdataExprs implements ExprFunctionProvider {

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "dataset",
        synopsis = "dataset(\"dataset:profile\")",
        description = "Return the TestDataView for the named dataset profile."
    )
    public TestDataView dataset(String datasetNameAndProfile) {
        return VectorTestData.catalogs().configure().catalog().profile(datasetNameAndProfile);
    }
    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "baseVectors",
        synopsis = "baseVectors(\"dataset:profile\")",
        description = "Return the BaseVectors associated with the dataset profile."
    )
    public BaseVectors baseVectors(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).getBaseVectors().orElseThrow(() ->
            new RuntimeException("Base vectors are not defined for dataset profile '" + datasetNameAndProfile + "'"));
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "queryVectors",
        synopsis = "queryVectors(\"dataset:profile\")",
        description = "Return the QueryVectors associated with the dataset profile."
    )
    public QueryVectors queryVectors(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).getQueryVectors().orElseThrow(() ->
            new RuntimeException("Query vectors are not defined for dataset profile '" + datasetNameAndProfile + "'"));
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "neighborDistances",
        synopsis = "neighborDistances(\"dataset:profile\")",
        description = "Return the NeighborDistances associated with the dataset profile."
    )
    public NeighborDistances neighborDistances(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).getNeighborDistances().orElseThrow(() ->
            new RuntimeException("Neighbor distances are not defined for dataset profile '" + datasetNameAndProfile + "'"));
    }

    @ExprExample(args = {"\"airports:demo\""}, expectNotNull = true)
    @ExprExample(args = {"\"airports:demo\""}, matches = ".+" )
    @ExprFunctionSpec(
        name = "neighborIndices",
        synopsis = "neighborIndices(\"dataset:profile\")",
        description = "Return the NeighborIndices associated with the dataset profile."
    )
    public NeighborIndices neighborIndices(String datasetNameAndProfile) {
        return dataset(datasetNameAndProfile).getNeighborIndices().orElseThrow(() ->
            new RuntimeException("Neighbor indices are not defined for dataset profile '" + datasetNameAndProfile + "'"));
    }

}
