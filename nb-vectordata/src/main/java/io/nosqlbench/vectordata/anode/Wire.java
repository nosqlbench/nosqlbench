/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata.anode;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

/// Little-endian primitives shared by the MNode and PNode wire codecs.
final class Wire {
    private Wire() { }

    static void u16(ByteArrayOutputStream out, int value) { out.write(value & 0xff); out.write((value >>> 8) & 0xff); }
    static void i16(ByteArrayOutputStream out, int value) { u16(out, value); }
    static void i32(ByteArrayOutputStream out, int value) { for (int i = 0; i < 4; i++) out.write((value >>> (8 * i)) & 0xff); }
    static void u32(ByteArrayOutputStream out, int value) { i32(out, value); }
    static void i64(ByteArrayOutputStream out, long value) { for (int i = 0; i < 8; i++) out.write((int) ((value >>> (8 * i)) & 0xff)); }

    /// A `u32`-prefixed UTF-8 string.
    static void string(ByteArrayOutputStream out, String value) {
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        u32(out, utf8.length);
        out.write(utf8, 0, utf8.length);
    }

    /// A `u32` read as a non-negative length; anything wider than an
    /// int is not a length this codec can buffer.
    static int u32(ByteBuffer in) {
        long value = Integer.toUnsignedLong(in.getInt());
        if (value > Integer.MAX_VALUE) throw new ANodeException("length " + value + " is too large");
        return (int) value;
    }

    static byte[] bytes(ByteBuffer in, int length) {
        if (length > in.remaining()) throw new ANodeException("truncated: " + length + " bytes wanted, " + in.remaining() + " left");
        byte[] out = new byte[length];
        in.get(out);
        return out;
    }

    static String string(ByteBuffer in, int length) {
        try { return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes(in, length))).toString(); }
        catch (CharacterCodingException invalid) { throw new ANodeException("invalid UTF-8: " + invalid.getMessage()); }
    }
}
