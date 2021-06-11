package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashRange;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferSizedHashedTest {

    @Test
    public void testWithHashRange() {
        LongToIntFunction sizeFunc = new HashRange(100, 1000);
        long input = 233423L;

        ByteBufferSizedHashed d1 = new ByteBufferSizedHashed(sizeFunc);
        ByteBuffer buf = d1.apply(input);
        assertThat(sizeFunc.applyAsInt(233423L)).isEqualTo(buf.remaining());
    }

}
