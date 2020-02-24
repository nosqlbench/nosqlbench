package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class DigestToByteBufferTest {

    @Test
    public void testWithMD5() {
        DigestToByteBuffer d1 = new DigestToByteBuffer(MessageDigestAlgorithms.MD5);
        ByteBuffer digest = d1.apply(233423L);
        System.out.println(Hex.encodeHexString(digest));
        byte[] bytes;
        try {
            bytes = Hex.decodeHex("8413891ca0f1e9e927c480b72a3844e6");
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        assertThat(digest).isEqualTo(ByteBuffer.wrap(bytes));
    }

    @Test
    public void testWithSHA1() {
        DigestToByteBuffer d1 = new DigestToByteBuffer(MessageDigestAlgorithms.SHA_1);
        ByteBuffer digest = d1.apply(233423L);
        System.out.println(Hex.encodeHexString(digest));
        byte[] bytes;
        try {
            bytes = Hex.decodeHex("2cffb2670c40af12487f5ecb39f394f1556bd4c8");
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        assertThat(digest).isEqualTo(ByteBuffer.wrap(bytes));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidName() {
        DigestToByteBuffer d1 = new DigestToByteBuffer("Whoops");
        ByteBuffer digest = d1.apply(233423L);
        assertThat(digest).isEqualTo(ByteBuffer.wrap(new byte[] {0x32}));
    }

}