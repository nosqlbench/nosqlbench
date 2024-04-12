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

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfDatasetsToString implements LongFunction<String> {
    private final HdfFile hdfFile;
    private final Dataset DSLeft;
    private final Dataset DSRight;
    private final String intraSeparator;
    private final String interSeparator;

    public HdfDatasetsToString(String filename, String DSNameLeft, String DSNameRight, String intraSeparator, String interSeparator) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        DSLeft = hdfFile.getDatasetByPath(DSNameLeft);
        DSRight = hdfFile.getDatasetByPath(DSNameRight);
        this.intraSeparator = intraSeparator;
        this.interSeparator = interSeparator;
    }

    /*
     * Read the column names from the columns DS and store them in an array
     * Read the column data types from the columnTypes DS and store them in an array
     * Create a csv schema string from the column names and data types
     */
    @Override
    public String apply(long value) {
        Object columnDataset = DSLeft.getData();
        Object columnTypeDataset = DSRight.getData();
        return pairByOrdinal((String[]) columnDataset, (String[])columnTypeDataset);
    }

    private String pairByOrdinal(String[] columnDataset, String[] columnTypeDataset) {
        if (columnDataset.length != columnTypeDataset.length) {
            throw new RuntimeException("Left hand dataset and right hand dataset must be the same length");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnDataset.length; i++) {
            sb.append(columnDataset[i]).append(intraSeparator).append(columnTypeDataset[i]);
            if (i < columnDataset.length - 1) {
                sb.append(interSeparator);
            }
        }
        return sb.toString();
    }
}
