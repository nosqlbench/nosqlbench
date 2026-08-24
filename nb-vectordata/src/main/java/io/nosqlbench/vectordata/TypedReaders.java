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

import io.nosqlbench.vectordata.internal.ElementCodec;

import java.lang.reflect.Array;
import java.net.URI;

/** Opens numeric projections for scalar and fixed-vector payloads. */
public final class TypedReaders {
    private TypedReaders() { }
    public static TypedReader open(URI source, VectorDataSettings settings, String identity) {
        return new Projection(VectorReaders.open(source, settings, identity));
    }
    private record Projection(VectorReader<?> reader) implements TypedReader {
        @Override public long count() { return reader.count(); }
        @Override public int dimension() { return reader.dimension(); }
        @Override public ElementType elementType() { return reader.elementType(); }
        @Override public Number scalar(long index) {
            if (reader.dimension() != 1 || !(reader.get(index) instanceof Number number))
                throw new VectorDataException("Source is not a packed scalar payload");
            return number;
        }
        @Override public Number[] vector(long index) {
            Object values = reader.get(index);
            if (values instanceof Number number) return new Number[] {number};
            int length = Array.getLength(values); Number[] result = new Number[length];
            for (int i = 0; i < length; i++) result[i] = number(values, i, reader.elementType());
            return result;
        }
        @Override public void prebuffer(PrebufferProgress progress) { reader.prebuffer(progress); }
        @Override public CacheStats cacheStats() { return reader.cacheStats(); }
        private static Number number(Object values, int index, ElementType type) {
            return switch (type) {
                case U8 -> Byte.toUnsignedInt(((byte[]) values)[index]); case I8 -> ((byte[]) values)[index];
                case U16 -> Short.toUnsignedInt(((short[]) values)[index]); case I16 -> ((short[]) values)[index];
                case U32 -> Integer.toUnsignedLong(((int[]) values)[index]); case I32 -> ((int[]) values)[index];
                case U64 -> UnsignedLong.ofBits(((long[]) values)[index]); case I64 -> ((long[]) values)[index]; case F16 -> ElementCodec.halfToFloat(((short[]) values)[index]);
                case F32 -> ((float[]) values)[index]; case F64 -> ((double[]) values)[index];
            };
        }
    }
}
