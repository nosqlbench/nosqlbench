package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class ToByteBufferTest {

    @Test
    public void toByteBuffer7() {
        ToByteBuffer f = new ToByteBuffer(7);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

    @Test
    public void toByteBuffer8() {
        ToByteBuffer f = new ToByteBuffer(8);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 33});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

    @Test
    public void toByteBuffer9() {
        ToByteBuffer f = new ToByteBuffer(9);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 33, 0});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

}