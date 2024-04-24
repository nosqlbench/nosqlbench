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

package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Replace all occurrences of the extant string with the replacement string.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ReplaceAll implements Function<String, String> {

    private final String extant;
    private final String replacement;

    @Example({"ReplaceAll('one','two')", "Replace all occurrences of 'one' with 'two'"})
    public ReplaceAll(String extant, String replacement) {
        this.extant = extant;
        this.replacement = replacement;
    }

    @Override
    public String apply(String s) {
        return s.replaceAll(extant, replacement);
    }
}
