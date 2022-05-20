package io.nosqlbench.virtdata.library.curves4.discrete.common;

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


import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

/**
 * This uses the Murmur3F (64-bit optimized) version of Murmur3,
 * not as a checksum, but as a simple hash. It doesn't bother
 * pushing the high-64 bits of input, since it only uses the lower
 * 64 bits of output. It does, however, return the absolute value.
 * This is to make it play nice with users and other libraries.
 */
public class ThreadSafeHash implements LongUnaryOperator {

//    private ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
//    private Murmur3F murmur3F= new Murmur3F();
    private static ThreadLocal<HashState> tlstate = ThreadLocal.withInitial(HashState::new);

    @Override
    public long applyAsLong(long value) {
        HashState state = tlstate.get();
        state.murmur3F.reset();
        state.byteBuffer.putLong(0,value);
//        bb.position(0);
        state.murmur3F.update(state.byteBuffer.array(),0,Long.BYTES);
        long result= Math.abs(state.murmur3F.getValue());
        return result;
    }

    private static class HashState {
        public Murmur3F murmur3F;
        public ByteBuffer byteBuffer;
        public HashState() {
            murmur3F = new Murmur3F();
            byteBuffer = ByteBuffer.allocate(Long.BYTES);
        }
    }

}
