package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Convert the contents of the input ByteBuffer to a String as hexadecimal.
 */
@ThreadSafeMapper
public class ByteBufferToHex implements Function<ByteBuffer,String> {
    @Override
    public String apply(ByteBuffer byteBuffer) {
        return Hex.encodeHexString(byteBuffer);
    }
}
