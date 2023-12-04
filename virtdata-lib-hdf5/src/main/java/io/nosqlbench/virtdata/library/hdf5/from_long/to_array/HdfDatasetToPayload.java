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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_array;


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.hdf5.from_long.AbstractHdfFileToVectorType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories(Category.experimental)
public class HdfDatasetToPayload extends AbstractHdfFileToVectorType implements LongFunction<Object[]> {
    private final Map<String,Class> typeMap = new HashMap<>();

    public HdfDatasetToPayload(String filename, String datasetName, String columnDS, String columnTypeDS) {
        super(filename, datasetName);
        populateTypeMap(columnDS, columnTypeDS);
    }

    private void populateTypeMap(String columnDS, String columnTypeDS) {
        Object columnDataset = hdfFile.getDatasetByPath(columnDS).getData();
        Object columnTypeDataset = hdfFile.getDatasetByPath(columnTypeDS).getData();
        String[] columnNames = (String[]) columnDataset;
        String[] columnTypes = (String[]) columnTypeDataset;
        if (columnNames.length != columnTypes.length) {
            throw new RuntimeException("Left hand dataset and right hand dataset must be the same length");
        }
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            String columnType = columnTypes[i];
            Class<?> type = switch (columnType) {
                case "int" -> int.class;
                case "long" -> long.class;
                case "float" -> float.class;
                case "double" -> double.class;
                case "String" -> String.class;
                default -> throw new RuntimeException("Unsupported type: " + columnType);
            };
            typeMap.put(columnName, type);
        }
    }

    @Override
    public Object[] apply(long value) {
        return new Object[0];
    }
}
