/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_array;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.hdf5.from_long.AbstractHdfFileToVectorType;

import java.util.function.LongFunction;

/**
 * This function reads a vector dataset from an HDF5 file. The dataset itself is not
 * read into memory, only the metadata (the "dataset" Java Object). The lambda function
 * reads a single vector from the dataset, based on the long input value. As currently
 * written this class will only work for datasets with 2 dimensions where the 1st dimension
 * specifies the number of vectors and the 2nd dimension specifies the number of elements in
 * each vector. Only datatypes short, int, and float are supported at this time.
 * <p>
 * This implementation is specific to returning an array of ints from a dataset that contains
 * variable length arrays of ints.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfFileToVarLengthIntArray extends AbstractHdfFileToVectorType implements LongFunction<int[]> {

    public HdfFileToVarLengthIntArray(String filename, String datasetName) {
        super(filename, datasetName);
    }
    @Override
    public int[] apply(long l) {
        Object data = getDataFrom(l);
        return extractIds(data, l);
    }

    /**
     * @param o The dataset from hdf5 file. In this case because the number of ints in each array is variable length it
     *          is returned by hdf5 as a one dimensional array of Objects
     * @param l The current cycle
     * @return The int array at the offset of l
     */
    private int[] extractIds(Object o, long l) {
        Object[] objects = (Object[]) o;
        Object o1 = objects[(int) l % objects.length];
        return (int[]) o1;
    }

}
