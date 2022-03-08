/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_charbuffer.CharBufferExtract;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedLoremExtractToString;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

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
