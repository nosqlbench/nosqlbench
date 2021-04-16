package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.Hash;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a CharBuffer from the first function, and then sample data from
 * that buffer according to the size function. The initFunction can be
 * given as simply a size, in which case ByteBufferSizedHash is used with Hex String
 * conversion.
 * If the size function yields a size larger than the available buffer size, then it is
 * lowered to that size automatically. If it is lower, then a random offset
 * is used within the buffer image.
 *
 * This function behaves slightly differently than most in that it creates and
 * caches as source byte buffer during initialization.
 */
public class HashedCharBufferExtract implements LongFunction<CharBuffer> {

    private final LongToIntFunction sizefunc;
    private final ThreadLocal<CharBuffer> bbaccessor;
    private final Hash inthash = new Hash();

    public HashedCharBufferExtract(Object initFunc, Object sizeFunc) {
        CharBuffer image = null;
        if (initFunc instanceof Number) {
            int bufsize = ((Number)initFunc).intValue();
            ByteBufferSizedHashed bufgen = new ByteBufferSizedHashed(bufsize);
            ByteBuffer bbimage = bufgen.apply(0).asReadOnlyBuffer();
            image = CharBuffer.wrap(Hex.encodeHex(bbimage));
        } else {
            LongFunction<String> bbfunc = VirtDataConversions.adaptFunction(initFunc, LongFunction.class, String.class);
            image = CharBuffer.wrap(bbfunc.apply(0));
        }
        CharBuffer finalImage = image;
        bbaccessor = ThreadLocal.withInitial(() -> finalImage.asReadOnlyBuffer());

        if (sizeFunc instanceof Number) {
            int size = ((Number)sizeFunc).intValue();
            this.sizefunc = l -> size;
        } else {
            this.sizefunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
    }

    @Override
    public CharBuffer apply(long value) {
        CharBuffer bbimage = bbaccessor.get();
        int newbufsize = sizefunc.applyAsInt(value);
        newbufsize=Math.min(newbufsize,bbimage.capacity());
        char[] chars = new char[newbufsize];
        int base_offset = inthash.applyAsInt(value) % (bbimage.capacity()-chars.length);
        bbaccessor.get().position(base_offset).get(chars);
        return CharBuffer.wrap(chars);
    }
}
