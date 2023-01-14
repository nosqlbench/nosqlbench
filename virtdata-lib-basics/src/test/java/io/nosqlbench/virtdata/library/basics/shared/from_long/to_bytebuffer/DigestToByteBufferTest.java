/*
 * Copyright (c) 2022-2023 nosqlbench
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

import static org.assertj.core.api.Assertions.*;

class DigestToByteBufferTest {

    @Test
    void testWithMD5() {
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
    void testWithSHA1() {
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
    void testInvalidNames() {

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new DigestToByteBuffer("Whoops"));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new DigestToByteBuffer(""));

    }

    @Test
    void testInstances() {

        DigestToByteBuffer sha256 = new DigestToByteBuffer("SHA-256");
        DigestToByteBuffer sha512 = new DigestToByteBuffer("SHA-512");

        try {
            ByteBuffer sha256Digest = sha256.apply(8675309L);
            ByteBuffer sha512Digest = sha512.apply(8675309L);

            byte[] bytesFromSha256;
            byte[] bytesFromSha512;
            try {
                bytesFromSha256 = Hex.decodeHex("4b74fe6b7d11205bf8714425d30e8d89f994d7b9e381622a8f419619c156bea990708b7e8e7eea47854a81e5aa00c2a16dfa7d75e0f57961be51215a2b9f255b");
                bytesFromSha512 = Hex.decodeHex("4b74fe6b7d11205bf8714425d30e8d89f994d7b9e381622a8f419619c156bea990708b7e8e7eea47854a81e5aa00c2a16dfa7d75e0f57961be51215a2b9f255b");

            } catch (DecoderException e) {
                throw new RuntimeException(e);
            }

            // System.out.println(Hex.encodeHexString(sha256Digest));
            // System.out.println(Hex.encodeHexString(sha512Digest));

            assertThat(sha256Digest).isEqualTo(ByteBuffer.wrap(bytesFromSha256));
            assertThat(sha512Digest).isEqualTo(ByteBuffer.wrap(bytesFromSha512));
        } catch(Exception e) {
            fail("unexpected exception found.");
        }

    }

}
