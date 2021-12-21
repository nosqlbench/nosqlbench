package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

@Categories(Category.conversion)
@ThreadSafeMapper
public class ToBase64String implements Function<String,String> {

    private final transient static ThreadLocal<TLState> tl_state = ThreadLocal.withInitial(TLState::new);

    @Example({"ToBase64String()","encode any input as Base64"})
    public ToBase64String() {
    }

    @Override
    public String apply(String value) {
        TLState state = tl_state.get();
        ByteBuffer sb = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
        return state.encoder.encodeToString(sb.array());
    }

    private static class TLState {
        public Base64.Encoder encoder = Base64.getEncoder();
    }
}
