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

package io.nosqlbench.datamappers.functions.long_localdate;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.time.LocalDate;
import java.util.function.LongFunction;

/**
 * Days since Jan 1st 1970
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class LongToLocalDateDays implements LongFunction<LocalDate> {

    @Example({"LongToLocalDateDays()","take the cycle number and turn it into a LocalDate based on days since 1970"})
    public LongToLocalDateDays (){
    }

    @Override
    public LocalDate apply(long value) {
        return LocalDate.ofEpochDay((int) value & Integer.MAX_VALUE);
   }

}
