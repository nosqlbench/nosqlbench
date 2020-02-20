package io.virtdata.libbasics.shared.from_long.to_bytebuffer;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Categories(Category.conversion)
@ThreadSafeMapper
public class DigestToByteBuffer implements LongFunction<ByteBuffer> {

    private static ThreadLocal<TL_State> tl_state;

    public DigestToByteBuffer(String digestType) {

        for (String digestName : MessageDigestAlgorithms.values()) {
            if (digestName.equals(digestType)) {
                Supplier<MessageDigest> mds = () -> getDigest(digestName);
                tl_state = ThreadLocal.withInitial(() -> new TL_State(mds));
                break;
            }
        }
        if (tl_state==null) {
            tl_state = ThreadLocal.withInitial(() -> new TL_State(() -> getDigest(digestType)));
        }
    }

    private static MessageDigest getDigest(String type) {
        try {
            return MessageDigest.getInstance(type);
        } catch (Exception e) {
            throw new RuntimeException("A digest of type " + type + " was not found. Select a digest type from: " +
                    Arrays.stream(MessageDigestAlgorithms.values()).collect(Collectors.joining(",", "[", "]")));
        }
    }

    @Override
    public ByteBuffer apply(long value) {
        TL_State state = tl_state.get();
        state.buf.putLong(0,value);
        byte[] digest = state.digest.digest(state.buf.array());
        return ByteBuffer.wrap(digest);
    }

    private final static class TL_State {
        private final MessageDigest digest;
        private final ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);

        public TL_State(Supplier<MessageDigest> mds) {
            digest = mds.get();
        }
    }
}
