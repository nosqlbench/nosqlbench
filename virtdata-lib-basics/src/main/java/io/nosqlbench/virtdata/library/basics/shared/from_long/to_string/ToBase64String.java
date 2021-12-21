package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.function.LongFunction;

/**
 * Computes the Base64 representation of the byte image of the input long.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToBase64String implements LongFunction<String> {

    private final transient static ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"ToBase64String()","Convert the bytes of a long input into a base64 String"})
    public ToBase64String() {
    }

    @Override
    public String apply(long value) {
        TLState state = tl_state.get();
        state.bytes.putLong(0,value);
        return state.encoder.encodeToString(state.bytes.array());
    }

    private static class TLState {
        public Base64.Encoder encoder = Base64.getEncoder();
        public ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES);
    }
}
