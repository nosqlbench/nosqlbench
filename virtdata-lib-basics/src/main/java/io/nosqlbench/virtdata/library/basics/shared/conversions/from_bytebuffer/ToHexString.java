package io.nosqlbench.virtdata.library.basics.shared.conversions.from_bytebuffer;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
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

    /**
     * Convert the ByteBuffer's contents to a hex string using upper case by default.
     */
    public ToHexString() {
        this(true);
    }

    /**
     * Convert the ByteBuffer's contents to a hex string upper or lower case.
     */
    public ToHexString(boolean useUpperCase) {
        this.useUpperCase = useUpperCase;
    }

    @Override
    public String apply(ByteBuffer byteBuffer) {
        return Hex.encodeHexString(byteBuffer,useUpperCase);
    }
}
