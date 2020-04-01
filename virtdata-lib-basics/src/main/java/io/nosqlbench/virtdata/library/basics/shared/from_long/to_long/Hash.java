package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
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
@ThreadSafeMapper
public class Hash implements LongUnaryOperator {

    private ThreadLocal<Murmur3F> murmur3f_TL = ThreadLocal.withInitial(Murmur3F::new);

    @Override
    public long applyAsLong(long value) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        Murmur3F murmur3f = murmur3f_TL.get();
        murmur3f.reset();
        bb.putLong(0,value);
        murmur3f.update(bb.array(),0,Long.BYTES);
        long result= Math.abs(murmur3f.getValue());
        return result;
    }
}
