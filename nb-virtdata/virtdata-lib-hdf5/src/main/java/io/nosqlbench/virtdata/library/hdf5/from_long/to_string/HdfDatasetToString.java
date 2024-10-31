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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * This function reads a vector dataset from an HDF5 file. The entire dataset is parsed  into a single
 * String Object with the discreet values separated by the user supplied separator character. It is
 * intended for use only with small datasets where the entire dataset can be read into memory and there
 * is no need to read individual vectors from the dataset.
 * The lambda function simply returns the String representation of the dataset.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfDatasetToString implements LongFunction<String> {
    private final HdfFile hdfFile;
    private final Dataset dataset;
    private final String separator;
    private final String datasetAsString;

    /**
     * Create a new binding function that accepts a long input value for the cycle and returns a string representation
     * of the specified dataset
     * @param filename
     * @param dataset
     * @param separator
     */
    public HdfDatasetToString(String filename, String dataset, String separator) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        this.dataset = hdfFile.getDatasetByPath(dataset);
        this.separator = separator;
        this.datasetAsString = parseDataset();
    }

    public HdfDatasetToString(String filename, String dataset) {
        this(filename, dataset, ",");
    }

    private String parseDataset() {
        String[] columnDataset = (String[])dataset.getData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnDataset.length; i++) {
            sb.append(columnDataset[i]);
            if (i < columnDataset.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    @Override
    public String apply(long value) {
        return datasetAsString;
    }

}
