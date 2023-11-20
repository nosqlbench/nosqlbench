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

package io.nosqlbench.virtdata.library.hdf5.from_long;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.api.content.NBIO;

public abstract class AbstractHdfFileToVectorType {
    protected final HdfFile hdfFile;
    protected final Dataset dataset;
    protected final int[] dims;

    public AbstractHdfFileToVectorType(String filename, String datasetName) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        //TODO: implement a function to get the dataset by name only without needing the full path
        dataset = hdfFile.getDatasetByPath(datasetName);
        dims = dataset.getDimensions();
    }

    protected Object getDataFrom(long l) {
        long[] sliceOffset = new long[dims.length];
        sliceOffset[0] = (l % dims[0]);
        int[] sliceDimensions = new int[dims.length];
        sliceDimensions[0] = 1;
        // Do we want to give the option of reducing vector dimensions here?
        sliceDimensions[1] = dims.length > 1 ? dims[1] : 1;
        return dataset.getData(sliceOffset, sliceDimensions);
    }
}
