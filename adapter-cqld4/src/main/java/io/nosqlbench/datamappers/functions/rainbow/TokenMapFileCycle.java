/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.datamappers.functions.rainbow;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/**
 * Utility function used for advanced data generation experiments.
 */
@ThreadSafeMapper
@Categories({Category.experimental})
public class TokenMapFileCycle extends TokenMapFileBaseFunction {

    public TokenMapFileCycle(String filename, boolean loopdata, boolean ascending) {
        super(filename, loopdata, false, ascending);
    }

    @Override
    public long applyAsLong(int value) {
        TokenMapFileAPIService datasvc = tl_DataSvc.get();
        return datasvc.getCycle();
    }
}
