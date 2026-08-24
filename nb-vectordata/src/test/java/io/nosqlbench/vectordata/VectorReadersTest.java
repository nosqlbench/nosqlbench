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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VectorReadersTest {
    @TempDir Path temporary;
    @Test void readsFixedVectorsAndCopiesToCallerArray() throws Exception {
        Path source = FixtureSupport.fvec(temporary, "base.fvec", new float[][] {{1.5f, 2f}, {3f, 4.5f}});
        @SuppressWarnings("unchecked") VectorReader<float[]> reader = (VectorReader<float[]>) VectorReaders.open(source.toString());
        assertEquals(2, reader.count()); assertEquals(2, reader.dimension()); assertArrayEquals(new float[] {3f, 4.5f}, reader.get(1));
        float[] target = new float[2]; reader.get(0, target); assertArrayEquals(new float[] {1.5f, 2f}, target);
        assertTrue(reader.isComplete()); assertEquals(AccessMode.LOCAL, reader.cacheStats().accessMode());
    }
    @Test void readsPackedScalarsAndVariableVectors() throws Exception {
        Path scalar = FixtureSupport.scalarI32(temporary, "values.i32", -1, 7);
        VectorReader<?> scalars = VectorReaders.open(scalar.toString());
        assertEquals(-1, scalars.get(0)); assertEquals(7, scalars.get(1));
        TypedReader typed = TypedReaders.open(scalar.toUri(), VectorDataSettings.defaults(), "test");
        assertEquals(-1, typed.scalar(0)); assertEquals(7, typed.scalar(1));
        Path variable = FixtureSupport.vvec(temporary, "metadata.ivecs", new int[][] {{1}, {2, 3}});
        @SuppressWarnings("unchecked") VvecReader<int[]> vectors = (VvecReader<int[]>) VectorReaders.openVvec(variable.toUri(), VectorDataSettings.defaults(), "test");
        assertEquals(2, vectors.count()); assertEquals(2, vectors.dimensionAt(1)); assertArrayEquals(new int[] {2, 3}, vectors.get(1));
        Path variable64 = FixtureSupport.vvec64(temporary, "metadata64.ivecs", new int[][] {{4}, {5, 6}});
        @SuppressWarnings("unchecked") VvecReader<int[]> vectors64 = (VvecReader<int[]>) VectorReaders.openVvec(variable64.toUri(), VectorDataSettings.defaults(), "test");
        assertArrayEquals(new int[] {5, 6}, vectors64.get(1));
    }
    @Test void rejectsMalformedFixedRecord() throws Exception {
        Path source = FixtureSupport.fvec(temporary, "bad.fvec", new float[][] {{1f, 2f}});
        java.nio.file.Files.write(source, new byte[] {0}, java.nio.file.StandardOpenOption.APPEND);
        assertThrows(VectorDataException.class, () -> VectorReaders.open(source.toString()));
    }
    @Test void preservesUnsigned64ScalarValues() throws Exception {
        java.nio.ByteBuffer bytes = java.nio.ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN).putLong(-1L);
        Path source = temporary.resolve("largest.u64"); java.nio.file.Files.write(source, bytes.array());
        assertEquals("18446744073709551615", VectorReaders.open(source.toString()).get(0).toString());
        assertEquals("18446744073709551615", TypedReaders.open(source.toUri(), VectorDataSettings.defaults(), "test").scalar(0).toString());
    }
    @Test void projectsF16AndF64Vectors() throws Exception {
        java.nio.ByteBuffer half = java.nio.ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        half.putInt(2).putShort((short) 0x3c00).putShort((short) 0xc100); // 1.0, -2.5
        Path f16 = temporary.resolve("half.mvec"); java.nio.file.Files.write(f16, half.array());
        assertArrayEquals(new Number[] {1.0f, -2.5f}, TypedReaders.open(f16.toUri(), VectorDataSettings.defaults(), "test").vector(0));
        java.nio.ByteBuffer doubles = java.nio.ByteBuffer.allocate(20).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        doubles.putInt(2).putDouble(1.25).putDouble(-3.5);
        Path f64 = temporary.resolve("double.dvec"); java.nio.file.Files.write(f64, doubles.array());
        assertArrayEquals(new Number[] {1.25d, -3.5d}, TypedReaders.open(f64.toUri(), VectorDataSettings.defaults(), "test").vector(0));
    }
}
