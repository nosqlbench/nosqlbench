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

import io.nosqlbench.vectordata.AccessMode;
import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.RangeFill;
import io.nosqlbench.vectordata.VectorDataException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Read-only memory mapped local file storage. */
public final class MappedStorage implements ByteStorage {
    private final FileChannel channel;
    private final ByteBuffer data;
    private final long size;

    public MappedStorage(Path path) {
        try {
            channel = FileChannel.open(path, StandardOpenOption.READ);
            size = channel.size();
            data = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
        } catch (IOException e) {
            throw new VectorDataException("Cannot map vector source " + path, e);
        }
    }

    @Override public long size() { return size; }
    @Override public ByteBuffer read(long offset, int length) {
        bounds(offset, length);
        ByteBuffer copy = data.asReadOnlyBuffer();
        copy.position(Math.toIntExact(offset)).limit(Math.toIntExact(offset + length));
        return copy.slice().asReadOnlyBuffer();
    }
    private void bounds(long offset, int length) {
        if (offset < 0 || length < 0 || offset > size - length)
            throw new VectorDataException("Byte range outside source: offset=" + offset + ", length=" + length + ", size=" + size);
    }
    @Override public void prebuffer(PrebufferProgress progress) { progress.onProgress(size, size); }
    @Override public void prebufferRange(long byteStart, long byteEnd, PrebufferProgress progress) {
        long length = Math.max(0, Math.min(byteEnd, size) - byteStart);
        progress.onProgress(length, length);
    }
    @Override public RangeFill rangeFill(long byteStart, long byteEnd) { return null; }
    @Override public boolean rangeCapable() { return true; }
    @Override public boolean isComplete() { return true; }
    @Override public CacheStats stats() { return new CacheStats(AccessMode.LOCAL, size, size, 0, 0, 0, true); }
    @Override public void close() { try { channel.close(); } catch (IOException ignored) { } }
}
