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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.function.LongFunction;

/**
 * Computes the Base64 representation of the byte image of the input long.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToBase64String implements LongFunction<String> {

    private final transient static ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"ToBase64String()","Convert the bytes of a long input into a base64 String"})
    public ToBase64String() {
    }

    @Override
    public String apply(long value) {
        TLState state = tl_state.get();
        state.bytes.putLong(0,value);
        return state.encoder.encodeToString(state.bytes.array());
    }

    private static class TLState {
        public Base64.Encoder encoder = Base64.getEncoder();
        public ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES);
    }
}
