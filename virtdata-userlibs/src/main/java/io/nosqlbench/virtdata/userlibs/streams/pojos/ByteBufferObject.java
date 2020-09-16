package io.nosqlbench.virtdata.userlibs.streams.pojos;

import io.nosqlbench.virtdata.userlibs.streams.fillers.ByteBufferFillable;
import io.nosqlbench.virtdata.userlibs.streams.fillers.Fillable;

import java.nio.ByteBuffer;
import java.util.List;

public class ByteBufferObject implements ByteBufferFillable {

    private final ByteBuffer buffer;

    public ByteBufferObject(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }
    public ByteBufferObject(ByteBuffer source) {
        this.buffer = source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void fill(Iterable<ByteBuffer> source) {
        ByteBufferFillable.fillByteBuffer(this.buffer,source);
    }
}
