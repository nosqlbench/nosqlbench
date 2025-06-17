package io.nosqlbench.virtdata.library.xvec;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.function.LongFunction;

@Categories({Category.readers, Category.conversion})
@ThreadSafeMapper
public class BVecToFloatReader implements LongFunction<float[]>, AutoCloseable {

    private final AsynchronousFileChannel fileChannel;
    private final int dimension;
    private final long vectorSize;
    private final long totalVectors;

    public BVecToFloatReader(String filePath) throws IOException {
        this.fileChannel = AsynchronousFileChannel.open(Path.of(filePath), StandardOpenOption.READ);

        // Read the first dimension (4 bytes, little-endian)
        ByteBuffer dimBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        try {
            fileChannel.read(dimBuffer, 0).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        dimBuffer.flip();
        this.dimension = Integer.reverseBytes(dimBuffer.getInt());

        this.vectorSize = 4L + dimension; // 4 bytes for dimension + d bytes for uint8 vector
        long fileSize = fileChannel.size();
        this.totalVectors = fileSize / vectorSize;
    }

    public float[] apply(long index) {
        if (index < 0 || index >= totalVectors) {
            throw new IndexOutOfBoundsException("Vector index " + index + " out of bounds");
        }

        long offset = index * vectorSize;
        ByteBuffer buffer = ByteBuffer.allocate((int) vectorSize).order(ByteOrder.LITTLE_ENDIAN);

        try {
            fileChannel.read(buffer, offset).get();  // Wait for async read to complete
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read vector at index " + index, e);
        }

        buffer.flip();
        int dim = Integer.reverseBytes(buffer.getInt());

        if (dim != dimension) {
            throw new IllegalStateException("Inconsistent dimension at vector index " + index);
        }

        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = buffer.get() & 0xFF; // convert unsigned byte to float
        }

        return vector;
    }

    @Override
    public void close() throws Exception {
        this.fileChannel.close();
    }
}
