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
package io.nosqlbench.vectordata;

import java.math.BigInteger;

/** Exact unsigned 64-bit integer value, including values above {@link Long#MAX_VALUE}. */
public final class UnsignedLong extends Number implements Comparable<UnsignedLong> {
    private final long bits;
    private UnsignedLong(long bits) { this.bits = bits; }
    public static UnsignedLong ofBits(long bits) { return new UnsignedLong(bits); }
    public BigInteger toBigInteger() {
        BigInteger magnitude = BigInteger.valueOf(bits & Long.MAX_VALUE);
        return bits < 0 ? magnitude.setBit(63) : magnitude;
    }
    @Override public int intValue() { return (int) bits; }
    @Override public long longValue() { return bits; }
    @Override public float floatValue() { return toBigInteger().floatValue(); }
    @Override public double doubleValue() { return toBigInteger().doubleValue(); }
    @Override public int compareTo(UnsignedLong other) { return Long.compareUnsigned(bits, other.bits); }
    @Override public String toString() { return Long.toUnsignedString(bits); }
    @Override public boolean equals(Object value) { return value instanceof UnsignedLong other && bits == other.bits; }
    @Override public int hashCode() { return Long.hashCode(bits); }
}
