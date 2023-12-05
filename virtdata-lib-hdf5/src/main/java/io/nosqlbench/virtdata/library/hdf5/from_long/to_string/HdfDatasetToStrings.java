/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.hdf5.from_long.AbstractHdfFileToVectorType;

import java.util.Arrays;
import java.util.function.LongFunction;

/**
 * This function reads a dataset from an HDF5 file. The dataset itself is not
 * read into memory, only the metadata (the "dataset" Java Object). The lambda function
 * reads a single vector from the dataset, based on the long input value.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfDatasetToStrings extends AbstractHdfFileToVectorType implements LongFunction<String> {

    public HdfDatasetToStrings(String filename, String datasetName) {
        super(filename, datasetName);
    }
    @Override
    public String apply(long l) {
        long[] sliceOffset = new long[dims.length];
        sliceOffset[0] = (l % dims[0]);
        int[] sliceDimensions = new int[dims.length];
        sliceDimensions[0] = 1;
        if (dims.length > 1) {
            for (int i = 1; i < dims.length; i++) {
                sliceDimensions[i] = dims[i];
            }
        }
        String payload = null;
        switch(dataset.getJavaType().getSimpleName().toLowerCase()) {
            case "string" ->
                payload = ((String[])dataset.getData(sliceOffset, sliceDimensions))[0];
            case "int" ->
                payload = Arrays.toString(((int[][]) dataset.getData(sliceOffset, sliceDimensions))[0]);
            case "float" ->
                payload = Arrays.toString(((float[][]) dataset.getData(sliceOffset, sliceDimensions))[0]);
            case "short" ->
                payload = Arrays.toString(((short[][]) dataset.getData(sliceOffset, sliceDimensions))[0]);
            case "long" ->
                payload = Arrays.toString(((long[][]) dataset.getData(sliceOffset, sliceDimensions))[0]);
            case "double" ->
                payload = Arrays.toString(((double[][]) dataset.getData(sliceOffset, sliceDimensions))[0]);
            case "char" ->
                payload = String.valueOf(((char[][])dataset.getData(sliceOffset, sliceDimensions))[0]);
        }
        if (payload == null) {
            throw new RuntimeException("Unsupported datatype: " + dataset.getJavaType().getSimpleName());
        }
        payload = payload.replaceAll("\\[", "").replaceAll("\\]", "");
        return payload;
    }

}
