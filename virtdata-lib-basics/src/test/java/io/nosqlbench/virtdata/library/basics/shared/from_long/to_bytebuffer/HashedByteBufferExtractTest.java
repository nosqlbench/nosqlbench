package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_charbuffer.CharBufferExtract;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedLoremExtractToString;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.LongToIntFunction;

public class HashedByteBufferExtractTest {

    @Test
    public void read1MBBytesDefault() {
        HashedByteBufferExtract bbe = new HashedByteBufferExtract(1024*1024,(LongToIntFunction) l -> 10);
        for (int i = 0; i < 10; i++) {
            ByteBuffer a0 = bbe.apply(i);
            System.out.println(Hex.encodeHex(a0));
        }
    }

    @Test
    public void read1MBBytesFunction() {
        HashedByteBufferExtract bbe = new HashedByteBufferExtract(new ByteBufferSizedHashed(1024*1024),(LongToIntFunction) l -> 10);
        for (int i = 0; i < 10; i++) {
            ByteBuffer a0 = bbe.apply(i);
            System.out.println(Hex.encodeHex(a0));
        }
    }

    @Test
    public void read1MBChars() {
        CharBufferExtract bbe = new CharBufferExtract(1024*1024,(LongToIntFunction) l -> 10);
        for (int i = 0; i < 10; i++) {
            CharBuffer a0 = bbe.apply(i);
            System.out.println(a0.toString());
        }
    }

    @Test
    public void read1MBCharsFunction() {
        CharBufferExtract bbe = new CharBufferExtract(new HashedLoremExtractToString(1000,1000),(LongToIntFunction) l -> 10);
        for (int i = 0; i < 10; i++) {
            CharBuffer a0 = bbe.apply(i);
            System.out.println(a0.toString());
        }
    }

}
