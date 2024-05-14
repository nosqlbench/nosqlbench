/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.qdrant.pojos;

import io.qdrant.client.grpc.Points.SparseIndices;

import java.util.List;
import java.util.Objects;

/**
 * Helper class to store the vector name, vector values and sparse indices to be used for searching points.
 */
public class SearchPointsHelper {
    private String vectorName;
    private List<Float> vectorValues;
    private SparseIndices sparseIndices;

    public SearchPointsHelper(String vectorName, List<Float> vectorValues, SparseIndices sparseIndices) {
        this.vectorName = vectorName;
        this.vectorValues = vectorValues;
        this.sparseIndices = sparseIndices;
    }

    public SearchPointsHelper() {
    }

    public String getVectorName() {
        return vectorName;
    }

    public void setVectorName(String vectorName) {
        this.vectorName = vectorName;
    }

    public List<Float> getVectorValues() {
        return vectorValues;
    }

    public void setVectorValues(List<Float> vectorValues) {
        this.vectorValues = vectorValues;
    }

    public SparseIndices getSparseIndices() {
        return sparseIndices;
    }

    public void setSparseIndices(SparseIndices sparseIndices) {
        this.sparseIndices = sparseIndices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchPointsHelper that = (SearchPointsHelper) o;
        return getVectorName().equals(that.getVectorName()) && getVectorValues().equals(that.getVectorValues()) && Objects.equals(getSparseIndices(), that.getSparseIndices());
    }

    @Override
    public int hashCode() {
        int result = getVectorName().hashCode();
        result = 31 * result + getVectorValues().hashCode();
        result = 31 * result + Objects.hashCode(getSparseIndices());
        return result;
    }
}
