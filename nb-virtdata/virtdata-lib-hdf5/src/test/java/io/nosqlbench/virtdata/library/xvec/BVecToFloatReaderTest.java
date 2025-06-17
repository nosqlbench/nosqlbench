/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.xvec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BVecToFloatReaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testBasicFunctionality() throws Exception {
        // Create a test file with known data
        Path testFile = tempDir.resolve("test.bvec");
        int dimensions = 5;
        int recordCount = 10;
        writeBVecFile(testFile, dimensions, recordCount);

        // Test the constructor
        try (BVecToFloatReader reader = new BVecToFloatReader(testFile.toString())) {
            // Test reading a vector
            for (int i = 0; i < recordCount; i++) {
                float[] vector = reader.apply(i);

                // Verify the vector dimensions
                assertThat(vector).hasSize(dimensions);

                // Verify the vector values (should match what we wrote)
                for (int j = 0; j < dimensions; j++) {
                    assertThat(vector[j]).isEqualTo(i & 0xFF);
                }
            }
        }
    }

    @Test
    public void testOutOfBoundsIndex() throws Exception {
        // Create a test file with known data
        Path testFile = tempDir.resolve("test_bounds.bvec");
        int dimensions = 3;
        int recordCount = 5;
        writeBVecFile(testFile, dimensions, recordCount);

        // Test the constructor
        try (BVecToFloatReader reader = new BVecToFloatReader(testFile.toString())) {
            // Test reading with negative index
            assertThrows(IndexOutOfBoundsException.class, () -> reader.apply(-1));

            // Test reading with index equal to total vectors
            assertThrows(IndexOutOfBoundsException.class, () -> reader.apply(recordCount));

            // Test reading with index greater than total vectors
            assertThrows(IndexOutOfBoundsException.class, () -> reader.apply(recordCount + 10));
        }
    }

    @Test
    public void testInconsistentDimension() throws Exception {
        // Create a test file with inconsistent dimensions
        Path testFile = tempDir.resolve("test_inconsistent.bvec");
        writeBVecFileWithInconsistentDimensions(testFile);

        // Test the constructor
        try (BVecToFloatReader reader = new BVecToFloatReader(testFile.toString())) {
            // The first vector should read fine
            assertDoesNotThrow(() -> reader.apply(0));

            // The second vector has inconsistent dimensions and should throw an exception
            assertThrows(IllegalStateException.class, () -> reader.apply(1));
        }
    }

    @Test
    public void testThreadSafety() throws Exception {
        // Create a test file with known data
        Path testFile = tempDir.resolve("test_threadsafe.bvec");
        int dimensions = 4;
        int recordCount = 20;
        writeBVecFile(testFile, dimensions, recordCount);

        BVecToFloatReader reader = new BVecToFloatReader(testFile.toString());
        ExecutorService executor = null;

        try {
            int threadCount = 10;
            executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> exceptions = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                executor.submit(() -> {
                    try {
                        // Each thread reads a different vector
                        float[] result = reader.apply(threadId % recordCount);
                        assertNotNull(result);

                        // Read the same vector again to ensure consistency
                        float[] secondRead = reader.apply(threadId % recordCount);
                        assertArrayEquals(result, secondRead);
                    } catch (Throwable t) {
                        synchronized (exceptions) {
                            exceptions.add(t);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            if (!exceptions.isEmpty()) {
                throw new RuntimeException("Exceptions occurred during concurrent access", exceptions.get(0));
            }
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
            reader.close();
        }
    }

    @Test
    public void testFileNotFound() {
        // Test with a non-existent file
        assertThrows(IOException.class, () -> new BVecToFloatReader("non_existent_file.bvec"));
    }

    @Test
    public void testAutoCloseable() throws Exception {
        // Create a test file
        Path testFile = tempDir.resolve("test_close.bvec");
        writeBVecFile(testFile, 3, 5);

        // Test that the reader can be used with try-with-resources
        try (BVecToFloatReader reader = new BVecToFloatReader(testFile.toString())) {
            float[] vector = reader.apply(0);
            assertNotNull(vector);
        }

        // The file should be closed after the try-with-resources block
        // We can't directly test that the file is closed, but we can verify that
        // creating a new reader works, which would fail if the file was still locked
        assertDoesNotThrow(() -> {
            try (BVecToFloatReader reader = new BVecToFloatReader(testFile.toString())) {
                reader.apply(0);
            }
        });
    }

    private void writeBVecFile(Path filePath, int dimensions, int recordCount) throws IOException {
        byte[] data = generateTestBVecData(dimensions, recordCount);
        Files.write(filePath, data);
    }

    private byte[] generateTestBVecData(int dimensions, int recordCount) {
        int recordSize = Integer.BYTES + dimensions; // 4 bytes for dimension + d bytes for uint8 vector
        byte[] data = new byte[recordSize * recordCount];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < recordCount; i++) {
            buffer.putInt(Integer.reverseBytes(dimensions)); // Dimension is stored in little-endian
            for (int j = 0; j < dimensions; j++) {
                buffer.put((byte)(i & 0xFF)); // Use the record index as the value for all dimensions
            }
        }

        return data;
    }

    private void writeBVecFileWithInconsistentDimensions(Path filePath) throws IOException {
        // Create a file with two vectors, where the second has a different dimension
        int firstDimension = 4;
        int secondDimension = 5;

        int firstRecordSize = Integer.BYTES + firstDimension;
        int secondRecordSize = Integer.BYTES + secondDimension;

        byte[] data = new byte[firstRecordSize + secondRecordSize];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        // First vector
        buffer.putInt(Integer.reverseBytes(firstDimension));
        for (int j = 0; j < firstDimension; j++) {
            buffer.put((byte)1);
        }

        // Second vector with different dimension
        buffer.putInt(Integer.reverseBytes(secondDimension));
        for (int j = 0; j < secondDimension; j++) {
            buffer.put((byte)2);
        }

        Files.write(filePath, data);
    }
}
