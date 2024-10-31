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

package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

// The intention is to have a shared function that can simply mirror the input for cases where
// defaults and conditions are evaluated as part of an overall binding value.
// E.g. MirrorToString(<<foo:bar>>);
@ThreadSafeMapper
@Categories({Category.conversion})
public class MirrorToString implements Function<String, String> {

    private String input;

    private MirrorToString() {
    }

    public MirrorToString(String input) throws BasicError {
        if (input == null) {
            throw new BasicError("Must supply input String to mirror.");
        }
        this.input = input;
    }

    @Override
    public String apply(String placeholder) {
        return input;
    }
}
