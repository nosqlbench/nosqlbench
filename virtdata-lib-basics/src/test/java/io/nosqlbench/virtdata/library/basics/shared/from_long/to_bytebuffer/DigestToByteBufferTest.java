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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DigestToByteBufferTest {

    @Test
    public void testWithMD5() {
        DigestToByteBuffer d1 = new DigestToByteBuffer(MessageDigestAlgorithms.MD5);
        ByteBuffer digest = d1.apply(233423L);
        byte[] bytes;
        try {
            bytes = Hex.decodeHex("8413891ca0f1e9e927c480b72a3844e6");
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        assertThat(digest).isEqualTo(ByteBuffer.wrap(bytes));
        System.out.println(Hex.encodeHexString(digest));
    }

    @Test
    public void testWithSHA1() {
        DigestToByteBuffer d1 = new DigestToByteBuffer(MessageDigestAlgorithms.SHA_1);
        ByteBuffer digest = d1.apply(233423L);
        byte[] bytes;
        try {
            bytes = Hex.decodeHex("2cffb2670c40af12487f5ecb39f394f1556bd4c8");
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        assertThat(digest).isEqualTo(ByteBuffer.wrap(bytes));
        System.out.println(Hex.encodeHexString(digest));
    }

    @Test
    public void testInvalidName() {
        DigestToByteBuffer d1 = new DigestToByteBuffer("Whoops");
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> d1.apply(233423L));
    }

}
