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

package io.nosqlbench.virtdata.library.basics.shared.formatting;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Apply the Java String.format method to an incoming object.
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax">Java 8 String.format(...) javadoc</a>
 * Note: This function can often be quite slow, so more direct methods are generally preferrable.
 */
@ThreadSafeMapper
@Categories(Category.conversion)
public class Format implements Function<Object,String> {

    private final String format;

    @Example({"Format('Y')","Yield the formatted year from a Java date object."})
    public Format(String format) {
        this.format = format;
    }

    @Override
    public String apply(Object o) {
        return String.format(format,o);
    }
}
