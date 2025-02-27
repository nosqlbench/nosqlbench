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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Binding function that accepts a long input value for the cycle and returns a string consisting of the
 * CQL predicate parsed from a single record in an HDF5 dataset
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfBinToCql implements LongFunction<String> {
    private final HdfFile hdfFile;
    private final Dataset predicateDataset;

    private final BinToCqlProcessor processor;

    public HdfBinToCql(String filename, String predicateName, String... schema) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        predicateDataset = hdfFile.getDatasetByPath(predicateName);

        processor = switch (predicateDataset.getJavaType().getSimpleName().toLowerCase()) {
            case "string" -> new BinStringToCql(predicateDataset, schema);
            case "byte" -> new BinArrayToCql(predicateDataset, schema);
            default -> throw new RuntimeException("Unknown data type in predicate dataset: " + predicateDataset.getJavaType().getSimpleName());
        };
    }

    @Override
    public String apply(long l) {
        return processor.process(l);
    }

}
