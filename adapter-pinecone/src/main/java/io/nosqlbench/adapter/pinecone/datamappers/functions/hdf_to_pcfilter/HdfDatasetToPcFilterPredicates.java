/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.adapter.pinecone.datamappers.functions.hdf_to_pcfilter;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetFilter;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.FilteredDatasetParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.MultiConditionFilter;

import java.util.function.LongFunction;

/**
 * Binding function that accepts a long input value for the cycle and returns a string consisting of a
 * portion of the Pinecone filter predicate parsed from a single record in an HDF5 dataset
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfDatasetToPcFilterPredicates implements LongFunction<String> {
    private final HdfFile hdfFile;
    private final Dataset dataset;
    private final int recordCount;
    private final FilteredDatasetParser parser;
    private final DatasetFilter filter;

    /**
     * Create a new binding function that accepts a long input value for the cycle and returns a string
     * @param filename
     * @param datasetname
     * @param parsername
     * @param level
     * @param isValue
     */
    public HdfDatasetToPcFilterPredicates(String filename, String datasetname, String parsername, int level, boolean isValue) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        dataset = hdfFile.getDatasetByPath(datasetname);
        recordCount = dataset.getDimensions()[0];
        parser = DatasetParser.filteredParserFactory(parsername);
        filter = new MultiConditionFilter(level, isValue);
        parser.setFilter(filter);
    }

    @Override
    public String apply(long l) {
        long[] sliceOffset = {(l % recordCount)};
        int[] sliceDimensions = {1};
        String raw = ((String[])dataset.getData(sliceOffset, sliceDimensions))[0];
        return parser.parse(raw);
    }
}
