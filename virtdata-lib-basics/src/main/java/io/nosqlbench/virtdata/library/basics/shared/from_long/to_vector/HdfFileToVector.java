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
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.embedding.EmbeddingGenerator;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.embedding.EmbeddingGeneratorFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.LongFunction;

/**
 * This function reads a vector dataset from an HDF5 file. The dataset itself is not
 * read into memory, only the metadata (the "dataset" Java Object). The lambda function
 * reads a single vector from the dataset, based on the long input value. As currently
 * written this class will only work for datasets with 2 dimensions where the 1st dimension
 * specifies the number of vectors and the 2nd dimension specifies the number of elements in
 * each vector. Only datatypes short, int, and float are supported at this time.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfFileToVector implements LongFunction<List<Float>> {
    private final HdfFile hdfFile;
    private final Dataset dataset;
    private final int[] dims;
    private final EmbeddingGenerator embeddingGenerator;

    public HdfFileToVector(String filename, String datasetName) {
        hdfFile = new HdfFile(Paths.get(filename));
        //TODO: implement a function to get the dataset by name only without needing the full path
        dataset = hdfFile.getDatasetByPath(datasetName);
        dims = dataset.getDimensions();
        embeddingGenerator = EmbeddingGeneratorFactory.getGenerator(dataset.getJavaType().getSimpleName().toLowerCase());
    }
    @Override
    public List<Float> apply(long l) {
        long[] sliceOffset = new long[dims.length];
        sliceOffset[0] = (l % dims[0]);
        int[] sliceDimensions = new int[dims.length];
        sliceDimensions[0] = 1;
        // Do we want to give the option of reducing vector dimensions here?
        sliceDimensions[1] = dims[1];
        Object data = dataset.getData(sliceOffset, sliceDimensions);

        return embeddingGenerator.generateEmbeddingFrom(data, dims);
    }

}
