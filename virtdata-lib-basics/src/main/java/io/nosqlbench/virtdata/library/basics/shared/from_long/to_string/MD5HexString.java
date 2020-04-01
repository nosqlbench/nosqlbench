package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.LongFunction;

/**
 * Computes the MD5 digest of the byte image of the input long, and
 * returns it in hexadecimal String form.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class MD5HexString implements LongFunction<String> {

    private final MessageDigest md5;
    private static final ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"MD5String()","Convert a long input to an md5 digest over its bytes, and then to a hexadecimal string."})
    public MD5HexString() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String apply(long value) {
        TLState state = tl_state.get();
        state.bytes.putLong(0,value);
        byte[] digest = md5.digest(state.bytes.array());
        String hexDigest = Hex.encodeHexString(digest);
        return hexDigest;
    }

    private final static class TLState {
        public final ByteBuffer bytes = ByteBuffer.allocate(16);
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
