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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

@Categories(Category.conversion)
@ThreadSafeMapper
public class ToBase64String implements Function<String,String> {

    private final transient static ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"ToBase64String()","encode any input as Base64"})
    public ToBase64String() {
    }

    @Override
    public String apply(String value) {
        TLState state = tl_state.get();
        ByteBuffer sb = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
        return state.encoder.encodeToString(sb.array());
    }

    private static class TLState {
        public Base64.Encoder encoder = Base64.getEncoder();
    }
}
