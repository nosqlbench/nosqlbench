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

import io.nosqlbench.virtdata.library.hdf5.from_long.to_list.HdfFileToFloatList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;

@Tag("unit")
public class HdfDatasetToStringsTest {

    @Test
    public void testHdfFileToVector() {
        final String[] results = new String[]{
            "String 1",
            "String 2",
            "String 3",
            "String 4"
        };

        HdfDatasetToStrings hdfFileToVector = new HdfDatasetToStrings(
            "src/test/resources/hdf5_test_strings.h5",
            "/strings");

        String read;
        for (int i = 0; i < 4; i++) {
            read = hdfFileToVector.apply(i);
            assert (read.equals(results[i]));
        }
    }
}
