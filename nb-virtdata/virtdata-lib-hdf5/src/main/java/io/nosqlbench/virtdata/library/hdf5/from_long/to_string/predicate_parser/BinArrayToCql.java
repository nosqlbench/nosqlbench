/*
 * Copyright (c) 2025 nosqlbench
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

import io.jhdf.api.Dataset;

public class BinArrayToCql extends BinToCqlProcessor {
    private final int[] dims;
    private final Dataset dataset;

    public BinArrayToCql(Dataset predicateDataset, String[] schema) {
        super(schema);
        dataset = predicateDataset;
        dims = dataset.getDimensions();
    }

    @Override
    public String process(long l) {
        long[] sliceOffset = new long[dims.length];
        sliceOffset[0] = (l % dims[0]);
        int[] sliceDimensions = new int[dims.length];
        // We always want to read a single value
        sliceDimensions[0] = 1;
        sliceDimensions[1] = dims[1];
        Object data = dataset.getData(sliceOffset, sliceDimensions);
        byte[] bytes = ((byte[][]) data)[0];
        return parser.parse(bytes);
    }
}
