package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ToMD5ByteBufferTest {

    @Test
    public void testMD5String() {
        MD5HexString ms = new MD5HexString();
        String apply = ms.apply(3L);
        assertThat(apply).isEqualTo("1fb332efe1406a104b11ffa1fa04fa7a");
    }

}