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

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_any;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Convert the input object to a JSON string with Gson.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToJSONF implements LongFunction<String> {
    private final static Gson gson = new GsonBuilder().create();
    private final LongFunction<?> objfunc;

    public ToJSONF(LongFunction<Object> objFunc) {
       this.objfunc = VirtDataConversions.adaptFunction(objFunc, LongFunction.class, String.class);
    }



    @Override
    public String apply(long value) {
        Object object = objfunc.apply(value);
        return gson.toJson(object);
    }
}
