package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Hash;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a ByteBuffer from the first function, and then sample data from
 * that bytebuffer according to the size function. The initFunction can be
 * given as simply a size, in which case ByteBufferSizedHash is used.
 * If the size function yields a size larger than the available buffer size, then it is
 * lowered to that size automatically. If it is lower, then a random offset
 * is used within the buffer image.
 *
 * This function behaves slightly differently than most in that it creates and
 * caches as source byte buffer during initialization.
 */
public class HashedByteBufferExtract implements LongFunction<ByteBuffer> {

    private final LongToIntFunction sizefunc;
    private final ThreadLocal<ByteBuffer> bbaccessor;
    private final Hash inthash = new Hash();

    public HashedByteBufferExtract(Object initFunc, Object sizeFunc) {
        ByteBuffer image = null;
        if (initFunc instanceof Number) {
            int bufsize = ((Number)initFunc).intValue();
            ByteBufferSizedHashed bufgen = new ByteBufferSizedHashed(bufsize);
            image = bufgen.apply(0).asReadOnlyBuffer();
        } else {
            LongFunction<ByteBuffer> bbfunc = VirtDataConversions.adaptFunction(initFunc, LongFunction.class, ByteBuffer.class);
            image = bbfunc.apply(0).asReadOnlyBuffer();
        }
        ByteBuffer finalImage = image;
        bbaccessor = ThreadLocal.withInitial(() -> finalImage.asReadOnlyBuffer());

        if (sizeFunc instanceof Number) {
            int size = ((Number)sizeFunc).intValue();
            this.sizefunc = l -> size;
        } else {
            this.sizefunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
    }

    @Override
    public ByteBuffer apply(long value) {
        ByteBuffer bbimage = bbaccessor.get();
        int newbufsize = sizefunc.applyAsInt(value);
        newbufsize=Math.min(newbufsize,bbimage.capacity());
        byte[] bytes = new byte[newbufsize];
        int base_offset = inthash.applyAsInt(value) % (bbimage.capacity()-bytes.length);
        bbaccessor.get().position(base_offset).get(bytes);
        return ByteBuffer.wrap(bytes);
    }
}
