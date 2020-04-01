package io.nosqlbench.virtdata.library.curves4.discrete.common;

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
