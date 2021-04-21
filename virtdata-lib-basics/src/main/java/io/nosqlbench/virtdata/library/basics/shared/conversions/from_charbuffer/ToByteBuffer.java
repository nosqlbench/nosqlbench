package io.nosqlbench.virtdata.library.basics.shared.conversions.from_charbuffer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@ThreadSafeMapper
@Categories(Category.conversion)
public class ToByteBuffer implements Function<CharBuffer, ByteBuffer> {
    @Override
    public ByteBuffer apply(CharBuffer charBuffer) {
        byte[] bytes = charBuffer.toString().getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.wrap(bytes);
    }
}
