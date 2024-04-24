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

package io.nosqlbench.virtdata.testmappers;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
public class TestableTemplate implements LongFunction<String> {

    private final LongFunction<?>[] funcs;
    private final String separator;

    public TestableTemplate(String separator, LongFunction<?>... funcs) {
        this.funcs = funcs;
        this.separator = separator;
    }

    @Override
    public String apply(long value) {
        StringBuilder sb = new StringBuilder();
        for (LongFunction<?> func : funcs) {
            sb.append(func.apply(value).toString());
            sb.append(separator);
        }
        sb.setLength(sb.length()-separator.length());
        return sb.toString();
    }
}
