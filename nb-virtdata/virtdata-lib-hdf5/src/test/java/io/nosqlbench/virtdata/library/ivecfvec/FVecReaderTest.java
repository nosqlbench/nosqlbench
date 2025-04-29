package io.nosqlbench.virtdata.library.ivecfvec;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FVecReaderTest {

    @Test
    public void testThreadSafety() throws Exception {
        // Create a temporary fvec file for testing
        Path tempFile = Files.createTempFile("test", ".fvec");
        Files.write(tempFile, generateTestFVecData(4, 10)); // 4 dimensions, 10 records

        try (FVecReader reader = new FVecReader(tempFile.toString())) {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Throwable> exceptions = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                executor.submit(() -> {
                    try {
                        // Each thread reads a record
                        float[] result = reader.apply(threadId);
                        assertNotNull(result);
                        assertDoesNotThrow(() -> reader.apply(threadId));
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // Ensure no exceptions were thrown
            if (!exceptions.isEmpty()) {
                throw new RuntimeException("Exceptions occurred during concurrent access", exceptions.get(0));
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private byte[] generateTestFVecData(int dimensions, int recordCount) {
        int recordSize = Integer.BYTES + (dimensions * Float.BYTES);
        byte[] data = new byte[recordSize * recordCount];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        for (int i = 0; i < recordCount; i++) {
            buffer.putInt(dimensions); // Write dimensions
            for (int j = 0; j < dimensions; j++) {
                buffer.putFloat(i * 1.0f); // Write dummy float data
            }
        }

        return data;
    }
}
