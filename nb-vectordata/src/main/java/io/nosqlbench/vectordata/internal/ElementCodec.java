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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.UnsignedLong;
import io.nosqlbench.vectordata.VectorDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** Little-endian primitive decoding shared by all vector formats. */
public final class ElementCodec {
    private ElementCodec() { }
    public static Object decode(ByteBuffer source, ElementType type, int count) {
        ByteBuffer bytes = source.order(ByteOrder.LITTLE_ENDIAN);
        return switch (type) {
            case U8, I8 -> bytes.get(new byte[count]).array();
            case U16, I16, F16 -> { short[] result = new short[count]; for (int i = 0; i < count; i++) result[i] = bytes.getShort(); yield result; }
            case U32, I32 -> { int[] result = new int[count]; for (int i = 0; i < count; i++) result[i] = bytes.getInt(); yield result; }
            case U64, I64 -> { long[] result = new long[count]; for (int i = 0; i < count; i++) result[i] = bytes.getLong(); yield result; }
            case F32 -> { float[] result = new float[count]; for (int i = 0; i < count; i++) result[i] = bytes.getFloat(); yield result; }
            case F64 -> { double[] result = new double[count]; for (int i = 0; i < count; i++) result[i] = bytes.getDouble(); yield result; }
        };
    }
    public static Number number(ByteBuffer source, ElementType type) {
        ByteBuffer bytes = source.order(ByteOrder.LITTLE_ENDIAN);
        return switch (type) {
            case U8 -> Byte.toUnsignedInt(bytes.get()); case I8 -> bytes.get();
            case U16 -> Short.toUnsignedInt(bytes.getShort()); case I16 -> bytes.getShort();
            case U32 -> Integer.toUnsignedLong(bytes.getInt()); case I32 -> bytes.getInt();
            case U64 -> UnsignedLong.ofBits(bytes.getLong()); case I64 -> bytes.getLong();
            case F16 -> halfToFloat(bytes.getShort()); case F32 -> bytes.getFloat(); case F64 -> bytes.getDouble();
        };
    }
    public static float halfToFloat(short half) {
        int h = half & 0xffff, sign = (h & 0x8000) << 16, exponent = (h >>> 10) & 0x1f, fraction = h & 0x3ff;
        int bits;
        if (exponent == 0) {
            if (fraction == 0) bits = sign;
            else { while ((fraction & 0x400) == 0) { fraction <<= 1; exponent--; } exponent++; fraction &= ~0x400; bits = sign | ((exponent + 112) << 23) | (fraction << 13); }
        } else if (exponent == 31) bits = sign | 0x7f800000 | (fraction << 13);
        else bits = sign | ((exponent + 112) << 23) | (fraction << 13);
        return Float.intBitsToFloat(bits);
    }
    public static void copy(Object source, Object target) {
        if (source instanceof byte[] a && target instanceof byte[] b) System.arraycopy(a, 0, b, 0, a.length);
        else if (source instanceof short[] a && target instanceof short[] b) System.arraycopy(a, 0, b, 0, a.length);
        else if (source instanceof int[] a && target instanceof int[] b) System.arraycopy(a, 0, b, 0, a.length);
        else if (source instanceof long[] a && target instanceof long[] b) System.arraycopy(a, 0, b, 0, a.length);
        else if (source instanceof float[] a && target instanceof float[] b) System.arraycopy(a, 0, b, 0, a.length);
        else if (source instanceof double[] a && target instanceof double[] b) System.arraycopy(a, 0, b, 0, a.length);
        else throw new VectorDataException("Reader target array has incompatible element type");
    }
    public static int arrayLength(Object value) {
        if (value instanceof byte[] a) return a.length; if (value instanceof short[] a) return a.length;
        if (value instanceof int[] a) return a.length; if (value instanceof long[] a) return a.length;
        if (value instanceof float[] a) return a.length; if (value instanceof double[] a) return a.length;
        throw new VectorDataException("Not a primitive vector array: " + value);
    }
}
