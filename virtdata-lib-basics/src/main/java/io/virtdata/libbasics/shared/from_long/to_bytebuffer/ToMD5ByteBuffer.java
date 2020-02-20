package io.virtdata.libbasics.shared.from_long.to_bytebuffer;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.LongFunction;

/**
 * Converts the byte image of the input long to a MD5 digest in ByteBuffer form.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToMD5ByteBuffer implements LongFunction<ByteBuffer> {

    private final MessageDigest md5;
    private static final ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"MD5ByteBuffer()","convert the a input to an md5 digest of its bytes"})
    public ToMD5ByteBuffer() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuffer apply(long value) {
        TLState state = tl_state.get();
        state.md5.reset();
        state.bytes.putLong(0,value);
        byte[] digest = md5.digest(state.bytes.array());
        return ByteBuffer.wrap(digest);
    }

    private final static class TLState {
        public final ByteBuffer bytes = ByteBuffer.allocate(160);
        public final MessageDigest md5;
        public TLState() {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
