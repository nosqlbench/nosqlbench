package io.nosqlbench.virtdata.library.ivecfvec;

import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FVecReaderTest {

    @Test
    public void testThreadSafety() throws Exception {
        // Use a relative path in the current working directory
        Path localFile = Path.of("test.fvec");
        writeFVecFile(localFile, 4, 10); // 4 dimensions, 10 records

        FVecReader reader = new FVecReader(localFile.toString());
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
                        float[] result = reader.apply(threadId);
                        assertNotNull(result);
                        assertDoesNotThrow(() -> reader.apply(threadId));
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
            Files.deleteIfExists(localFile);
        }
    }

    private void writeFVecFile(Path filePath, int dimensions, int recordCount) throws IOException {
        byte[] data = generateTestFVecData(dimensions, recordCount);
        Files.write(filePath, data);
    }

    private byte[] generateTestFVecData(int dimensions, int recordCount) {
        int recordSize = Integer.BYTES + (dimensions * Float.BYTES);
        byte[] data = new byte[recordSize * recordCount];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < recordCount; i++) {
            buffer.putInt(dimensions);
            for (int j = 0; j < dimensions; j++) {
                buffer.putFloat(i * 1.0f);
            }
        }

        return data;
    }
}
