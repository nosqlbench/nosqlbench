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
import io.nosqlbench.vectordata.IntegrityException;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.RangeFill;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLong;

/** Persistent sparse HTTP range cache with optional `.mref` leaf verification. */
public final class RemoteStorage implements ByteStorage {
    private static final byte[] STATE_MAGIC = new byte[] {'N', 'B', 'V', 'D', '1'};
    private final URI uri;
    private final HttpTransport transport;
    private final long size;
    private final int chunkSize;
    private final MerkleReference reference;
    private final boolean ranges;
    private final Path dataPath;
    private final Path statePath;
    private final BitSet present;
    private final FileChannel data;
    private final Object lock = new Object();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private volatile boolean complete;

    public RemoteStorage(URI uri, VectorDataSettings settings, Path cachePath) {
        this.uri = uri;
        this.transport = new HttpTransport(settings);
        MerkleReference found = loadReference(uri);
        HttpTransport.Probe probe = transport.probe(uri);
        this.reference = found;
        this.size = found == null ? probe.contentLength() : found.contentSize();
        if (size < 0) throw new VectorDataException("Server did not provide a content length for " + uri);
        long configured = found == null ? settings.chunkSize() : found.chunkSize();
        if (configured > Integer.MAX_VALUE) throw new VectorDataException("Chunk size is too large for Java reader: " + configured);
        this.chunkSize = (int) configured;
        this.ranges = probe.ranges();
        this.dataPath = cachePath.resolveSibling(cachePath.getFileName() + ".data");
        this.statePath = cachePath.resolveSibling(cachePath.getFileName() + ".mrkl");
        try {
            Files.createDirectories(dataPath.getParent());
            data = FileChannel.open(dataPath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            present = loadState();
            complete = present.cardinality() == chunks();
        } catch (IOException e) { throw new VectorDataException("Cannot open cache for " + uri, e); }
    }

    private MerkleReference loadReference(URI source) {
        try { return MerkleReference.parse(transport.get(URI.create(source + ".mref")), source.toString()); }
        catch (HttpStatusException missing) {
            // Public S3 datasets may expose data while restricting optional sidecars.
            // Rust falls back to the non-Merkle range path in that situation.
            if (missing.status() == 401 || missing.status() == 403 || missing.status() == 404) return null;
            throw missing;
        }
    }
    @Override public long size() { return size; }
    @Override public ByteBuffer read(long offset, int length) {
        checkBounds(offset, length);
        if (length == 0) return ByteBuffer.allocate(0).asReadOnlyBuffer();
        ensure(offset / chunkSize, (offset + length - 1) / chunkSize);
        ByteBuffer result = ByteBuffer.allocate(length);
        synchronized (lock) {
            try { while (result.hasRemaining()) if (data.read(result, offset + result.position()) < 0) throw new IOException("Unexpected cache EOF"); }
            catch (IOException e) { throw new VectorDataException("Cannot read cache for " + uri, e); }
        }
        return result.flip().asReadOnlyBuffer();
    }
    private void ensure(long first, long last) {
        synchronized (lock) {
            for (long chunk = first; chunk <= last; chunk++) {
                if (present.get(Math.toIntExact(chunk))) hits.incrementAndGet(); else fetch(chunk);
            }
        }
    }
    private void fetch(long chunk) {
        long offset = chunk * (long) chunkSize;
        int length = Math.toIntExact(Math.min(chunkSize, size - offset));
        byte[] bytes;
        if (ranges) {
            bytes = transport.range(uri, offset, offset + length - 1);
            if (bytes.length != length) throw new VectorDataException("Range response length mismatch for " + uri + " chunk " + chunk);
        } else {
            bytes = transport.get(uri);
            if (bytes.length != size) throw new VectorDataException("Full HTTP response length mismatch for " + uri);
            write(0, bytes);
            for (int i = 0; i < chunks(); i++) verifyAndMark(i);
            saveState();
            return;
        }
        if (reference != null && !MessageDigest.isEqual(hash(bytes), reference.leaf(Math.toIntExact(chunk))))
            throw new IntegrityException("Merkle leaf verification failed for " + uri + " chunk " + chunk);
        write(offset, bytes);
        present.set(Math.toIntExact(chunk));
        misses.incrementAndGet();
        complete = present.cardinality() == chunks();
        saveState();
    }
    private void verifyAndMark(int chunk) {
        long offset = chunk * (long) chunkSize;
        int length = Math.toIntExact(Math.min(chunkSize, size - offset));
        if (reference != null && !MessageDigest.isEqual(hash(readCached(offset, length)), reference.leaf(chunk)))
            throw new IntegrityException("Merkle leaf verification failed for " + uri + " chunk " + chunk);
        present.set(chunk);
        misses.incrementAndGet();
        complete = present.cardinality() == chunks();
    }
    private byte[] readCached(long offset, int length) {
        ByteBuffer bytes = ByteBuffer.allocate(length);
        try { while (bytes.hasRemaining()) data.read(bytes, offset + bytes.position()); }
        catch (IOException e) { throw new VectorDataException("Cannot verify cache for " + uri, e); }
        return bytes.array();
    }
    private void write(long offset, byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try { while (buffer.hasRemaining()) data.write(buffer, offset + buffer.position()); data.force(false); }
        catch (IOException e) { throw new VectorDataException("Cannot write cache for " + uri, e); }
    }
    @Override public void prebuffer(PrebufferProgress progress) {
        for (int i = 0; i < chunks(); i++) { ensure(i, i); progress.onProgress(Math.min(size, (long) present.cardinality() * chunkSize), size); }
    }
    @Override public void prebufferRange(long byteStart, long byteEnd, PrebufferProgress progress) {
        long end = Math.min(byteEnd, size);
        if (byteStart < 0 || byteStart >= end) { progress.onProgress(0, 0); return; }
        int first = Math.toIntExact(byteStart / chunkSize);
        int last = Math.toIntExact((end - 1) / chunkSize);
        long rangeBytes = Math.min(size, (last + 1L) * chunkSize) - (long) first * chunkSize;
        synchronized (lock) {
            long done = 0;
            for (int chunk = first; chunk <= last; chunk++) {
                if (present.get(chunk)) hits.incrementAndGet(); else fetch(chunk);
                done += chunkLength(chunk);
                progress.onProgress(done, rangeBytes);
            }
        }
    }
    @Override public RangeFill rangeFill(long byteStart, long byteEnd) {
        if (size == 0 || byteEnd <= byteStart || byteStart < 0 || byteStart >= size) return null;
        long end = Math.min(byteEnd, size);
        int first = Math.toIntExact(byteStart / chunkSize);
        int last = Math.toIntExact(Math.min((end - 1) / chunkSize, chunks() - 1L));
        if (first > last) return null;
        int resident = 0;
        synchronized (lock) { for (int chunk = first; chunk <= last; chunk++) if (present.get(chunk)) resident++; }
        long alignedStart = (long) first * chunkSize;
        long alignedEnd = Math.min((last + 1L) * chunkSize, size);
        return new RangeFill(first, last, chunkSize, last - first + 1, resident, alignedStart, alignedEnd);
    }
    @Override public boolean rangeCapable() { return ranges; }
    private long chunkLength(int chunk) { return Math.min(chunkSize, size - (long) chunk * chunkSize); }
    @Override public boolean isComplete() { return complete; }
    @Override public CacheStats stats() {
        long cached = Math.min(size, (long) present.cardinality() * chunkSize);
        AccessMode mode = reference == null ? (ranges ? AccessMode.MERKLE_CHUNKED : AccessMode.FULL_TRANSFER) : AccessMode.MERKLE_HASHED;
        return new CacheStats(mode, size, cached, chunkSize, hits.get(), misses.get(), complete);
    }
    private int chunks() { return size == 0 ? 0 : Math.toIntExact((size + chunkSize - 1) / chunkSize); }
    private void checkBounds(long offset, int length) {
        if (offset < 0 || length < 0 || offset > size - length) throw new VectorDataException("Byte range outside remote source " + uri);
    }
    private BitSet loadState() throws IOException {
        if (!Files.isRegularFile(statePath)) return new BitSet(chunks());
        byte[] raw = Files.readAllBytes(statePath);
        if (raw.length < STATE_MAGIC.length + 16) return new BitSet(chunks());
        for (int i = 0; i < STATE_MAGIC.length; i++) if (raw[i] != STATE_MAGIC[i]) return new BitSet(chunks());
        ByteBuffer header = ByteBuffer.wrap(raw).position(STATE_MAGIC.length).slice();
        long storedSize = header.getLong(); int storedChunkSize = header.getInt(); int words = header.getInt();
        if (storedSize != size || storedChunkSize != chunkSize || words < 0 || raw.length != STATE_MAGIC.length + 16 + words * 8) return new BitSet(chunks());
        long[] values = new long[words]; for (int i = 0; i < words; i++) values[i] = header.getLong();
        return BitSet.valueOf(values);
    }
    private void saveState() {
        long[] bits = present.toLongArray();
        ByteBuffer output = ByteBuffer.allocate(STATE_MAGIC.length + 16 + bits.length * 8);
        output.put(STATE_MAGIC).putLong(size).putInt(chunkSize).putInt(bits.length); for (long bit : bits) output.putLong(bit);
        Path temporary = statePath.resolveSibling(statePath.getFileName() + ".tmp");
        try {
            Files.write(temporary, output.array());
            try { Files.move(temporary, statePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException ignored) { Files.move(temporary, statePath, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new VectorDataException("Cannot save cache state for " + uri, e); }
    }
    private static byte[] hash(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    @Override public void close() { try { data.close(); } catch (IOException ignored) { } }
}
