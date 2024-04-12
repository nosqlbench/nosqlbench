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

package io.nosqlbench.virtdata.library.basics.shared.util;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Arrays;
import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.diagnostics)
public class Stringify implements Function<Object,String> {
    @Override
    public String apply(Object o) {
        if (o instanceof float[] fary) {
            return Arrays.toString(fary);
        } else if (o instanceof double[] dary) {
            return Arrays.toString(dary);
        } else if (o instanceof long[] lary) {
            return Arrays.toString(lary);
        } else if (o instanceof int[] iary) {
            return Arrays.toString(iary);
        } else if (o instanceof Object[] oary) {
            return Arrays.toString(oary);
        } else if (o instanceof byte[] bary) {
            return Arrays.toString(bary);
        } else if (o instanceof boolean[] bary) {
            return Arrays.toString(bary);
        } else if (o instanceof char[] cary) {
            return Arrays.toString(cary);
        } else if (o instanceof short[] sary) {
            return Arrays.toString(sary);
        } else {
            return String.valueOf(o);
        }
    }
}
