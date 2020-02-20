package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.core.murmur.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.LongToIntFunction;

/**
 * This uses the Murmur3F (64-bit optimized) version of Murmur3,
 * not as a checksum, but as a simple hash. It doesn't bother
 * pushing the high-64 bits of input, since it only uses the lower
 * 64 bits of output. It does, however, return the absolute value.
 * This is to make it play nice with users and other libraries.
 */
@ThreadSafeMapper
public class Hash implements LongToIntFunction {

    ThreadLocal<ByteBuffer> bb_TL = ThreadLocal.withInitial(() -> ByteBuffer.allocate(Long.BYTES));
    ThreadLocal<Murmur3F> murmur3f_TL = ThreadLocal.withInitial(Murmur3F::new);

    @Override
    public int applyAsInt(long value) {
        Murmur3F murmur3F = murmur3f_TL.get();
        ByteBuffer bb = bb_TL.get();
        murmur3F.reset();
        bb.putLong(0,value);
        murmur3F.update(bb.array(),0,Long.BYTES);
        long result= Math.abs(murmur3F.getValue());
        return (int) (result & Integer.MAX_VALUE);
    }
}
