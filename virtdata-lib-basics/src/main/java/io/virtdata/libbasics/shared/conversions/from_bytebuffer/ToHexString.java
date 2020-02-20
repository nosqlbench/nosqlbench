package io.virtdata.libbasics.shared.conversions.from_bytebuffer;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Converts the input ByteBuffer to a hexadecimal String.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToHexString implements Function<ByteBuffer,String> {

    private final boolean useUpperCase;

    public ToHexString() {
        this(true);
    }

    public ToHexString(boolean useUpperCase) {
        this.useUpperCase = useUpperCase;
    }

    @Override
    public String apply(ByteBuffer byteBuffer) {
        return Hex.encodeHexString(byteBuffer,useUpperCase);
    }
}
