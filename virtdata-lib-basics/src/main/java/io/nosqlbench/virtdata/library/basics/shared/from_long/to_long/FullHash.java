package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

/**
 * This uses the Murmur3F (64-bit optimized) version of Murmur3,
 * not as a checksum, but as a simple hash. It doesn't bother
 * pushing the high-64 bits of input, since it only uses the lower
 * 64 bits of output.
 *
 * This version returns the value regardless of this sign bit.
 * It does not return the absolute value, as {@link Hash} does.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class FullHash implements LongUnaryOperator {

    private final ThreadLocal<State> state_TL = ThreadLocal.withInitial(State::new);

    @Override
    public long applyAsLong(long value) {
        State state = state_TL.get();
        state.murmur3F.reset();
        state.bb.putLong(0,value);
        state.murmur3F.update(state.bb.array(),0,Long.BYTES);
        return state.murmur3F.getValue();
    }

    private static class State {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        Murmur3F murmur3F = new Murmur3F();
    }
}
