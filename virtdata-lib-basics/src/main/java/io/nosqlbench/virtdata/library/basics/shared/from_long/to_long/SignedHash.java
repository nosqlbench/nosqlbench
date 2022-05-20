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
 * Unlike the other hash functions, this one may return positive
 * as well as negative values.
 */
@ThreadSafeMapper
public class SignedHash implements LongUnaryOperator {

    // TODO: Bench this against the non-state based TL implementations
    private ThreadLocal<State> state_TL = ThreadLocal.withInitial(State::new);

    @Override
    public long applyAsLong(long value) {
        State state = state_TL.get();
        state.murmur3F.reset();
        state.bb.putLong(0,value);
        state.murmur3F.update(state.bb.array(),0,Long.BYTES);
        long result= state.murmur3F.getValue();
        return result;
    }

    private static class State {
        public ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        public Murmur3F murmur3F = new Murmur3F();
    }
}
