package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class SignedHash implements IntUnaryOperator {

    private ThreadLocal<Murmur3F> murmur3f_TL = ThreadLocal.withInitial(Murmur3F::new);

    @Override
    public int applyAsInt(int operand) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        Murmur3F murmur3f = murmur3f_TL.get();
        murmur3f.reset();
        bb.putInt(0,operand);
        bb.putInt(4,operand);
        bb.position(0);
        murmur3f.update(bb.array());
        return (int) murmur3f.getValue() & Integer.MAX_VALUE;
    }
}
