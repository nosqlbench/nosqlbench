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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

final class FixtureSupport {
    private FixtureSupport() { }
    static Path fvec(Path directory, String filename, float[][] values) throws IOException {
        int dimension = values[0].length; ByteBuffer bytes = ByteBuffer.allocate(values.length * (4 + dimension * 4)).order(ByteOrder.LITTLE_ENDIAN);
        for (float[] value : values) { bytes.putInt(dimension); for (float element : value) bytes.putFloat(element); }
        Path result = directory.resolve(filename); Files.write(result, bytes.array()); return result;
    }
    static Path ivec(Path directory, String filename, int[][] values) throws IOException {
        int dimension = values[0].length; ByteBuffer bytes = ByteBuffer.allocate(values.length * (4 + dimension * 4)).order(ByteOrder.LITTLE_ENDIAN);
        for (int[] value : values) { bytes.putInt(dimension); for (int element : value) bytes.putInt(element); }
        Path result = directory.resolve(filename); Files.write(result, bytes.array()); return result;
    }
    static Path scalarI32(Path directory, String filename, int... values) throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN); for (int value : values) bytes.putInt(value);
        Path result = directory.resolve(filename); Files.write(result, bytes.array()); return result;
    }
    static Path vvec(Path directory, String filename, int[][] values) throws IOException {
        int size = 0; for (int[] value : values) size += 4 + value.length * 4;
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN); ByteBuffer index = ByteBuffer.allocate((values.length + 1) * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int[] value : values) { index.putInt(data.position()); data.putInt(value.length); for (int element : value) data.putInt(element); }
        index.putInt(data.position()); Path result = directory.resolve(filename); Files.write(result, data.array()); Files.write(directory.resolve("IDXFOR__" + filename + ".i32"), index.array()); return result;
    }
    static Path vvec64(Path directory, String filename, int[][] values) throws IOException {
        int size = 0; for (int[] value : values) size += 4 + value.length * 4;
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN); ByteBuffer index = ByteBuffer.allocate((values.length + 1) * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (int[] value : values) { index.putLong(data.position()); data.putInt(value.length); for (int element : value) data.putInt(element); }
        index.putLong(data.position()); Path result = directory.resolve(filename); Files.write(result, data.array()); Files.write(directory.resolve("IDXFOR__" + filename + ".i64"), index.array()); return result;
    }
    /// A single-namespace slab: `records` records, `perPage` to a data
    /// page, each record a little-endian ordinal followed by padding to
    /// `recordBytes`. Data pages are laid out in ordinal order and the
    /// file ends with the pages page, as the slabtastic writer lays a
    /// file out.
    static Path slab(Path directory, String filename, int records, int perPage, int recordBytes) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.util.List<byte[]> index = new java.util.ArrayList<>();
        slabDataPages(out, index, records, perPage, recordBytes, 1);
        out.write(slabPage(index, 0, 1, 1));
        Path result = directory.resolve(filename); Files.write(result, out.toByteArray()); return result;
    }
    /// A slab whose default namespace holds `records` records and whose
    /// namespace `name` holds `others`, ending with a namespaces page
    /// that locates each namespace's pages page.
    static Path slabWithNamespace(Path directory, String filename, int records, String name, int others, int perPage, int recordBytes) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.util.List<byte[]> first = new java.util.ArrayList<>();
        slabDataPages(out, first, records, perPage, recordBytes, 1);
        long firstPages = out.size(); out.write(slabPage(first, 0, 1, 1));
        java.util.List<byte[]> second = new java.util.ArrayList<>();
        slabDataPages(out, second, others, perPage, recordBytes, 2);
        long secondPages = out.size(); out.write(slabPage(second, 0, 1, 2));
        out.write(slabPage(java.util.List.of(namespaceEntry(1, "", firstPages), namespaceEntry(2, name, secondPages)), 0, 3, 1));
        Path result = directory.resolve(filename); Files.write(result, out.toByteArray()); return result;
    }
    private static void slabDataPages(java.io.ByteArrayOutputStream out, java.util.List<byte[]> index, int records, int perPage, int recordBytes, int namespaceIndex) throws IOException {
        for (int start = 0; start < records; start += perPage) {
            int count = Math.min(perPage, records - start);
            java.util.List<byte[]> page = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                ByteBuffer record = ByteBuffer.allocate(recordBytes).order(ByteOrder.LITTLE_ENDIAN); record.putInt(start + i);
                while (record.hasRemaining()) record.put((byte) 'x');
                page.add(record.array());
            }
            ByteBuffer entry = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN); entry.putLong(start).putLong(out.size()); index.add(entry.array());
            out.write(slabPage(page, start, 2, namespaceIndex));
        }
    }
    private static byte[] namespaceEntry(int index, String name, long pagesPageOffset) {
        byte[] utf8 = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteBuffer entry = ByteBuffer.allocate(2 + utf8.length + 8).order(ByteOrder.LITTLE_ENDIAN);
        entry.put((byte) index).put((byte) utf8.length).put(utf8).putLong(pagesPageOffset); return entry.array();
    }
    /// One page in the slab layout: magic, size, record data, the
    /// offset array, and the 16-byte footer.
    private static byte[] slabPage(java.util.List<byte[]> records, long startOrdinal, int pageType, int namespaceIndex) {
        int data = 0; for (byte[] record : records) data += record.length;
        int size = 8 + data + 4 * (records.size() + 1) + 16;
        ByteBuffer page = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        page.put(new byte[] {'S', 'L', 'A', 'B'}).putInt(size);
        for (byte[] record : records) page.put(record);
        int offset = 8; for (byte[] record : records) { page.putInt(offset); offset += record.length; } page.putInt(offset);
        for (int i = 0; i < 5; i++) page.put((byte) (startOrdinal >>> (8 * i)));
        int count = records.size(); page.put((byte) count).put((byte) (count >>> 8)).put((byte) (count >>> 16));
        page.putInt(size).put((byte) pageType).put((byte) namespaceIndex).putShort((short) 16);
        return page.array();
    }
    /** Serializes the vectordata-rs MerkleRef layout (complete, padded binary tree). */
    static byte[] mref(byte[] data, int chunkSize) throws Exception {
        int chunks = (data.length + chunkSize - 1) / chunkSize;
        int leaves = 1; while (leaves < Math.max(1, chunks)) leaves <<= 1;
        int internals = leaves - 1, nodes = internals + leaves;
        byte[][] hashes = new byte[nodes][]; MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int i = 0; i < leaves; i++) hashes[internals + i] = digest.digest(i < chunks ? java.util.Arrays.copyOfRange(data, i * chunkSize, Math.min(data.length, (i + 1) * chunkSize)) : new byte[0]);
        for (int i = internals - 1; i >= 0; i--) { digest.reset(); digest.update(hashes[i * 2 + 1]); hashes[i] = digest.digest(hashes[i * 2 + 2]); }
        ByteBuffer result = ByteBuffer.allocate(nodes * 32 + 45).order(ByteOrder.BIG_ENDIAN);
        for (byte[] hash : hashes) result.put(hash);
        result.putLong(chunkSize).putLong(data.length).putInt(chunks).putInt(leaves).putInt(leaves).putInt(nodes).putInt(internals).putInt(internals).putInt(0).put((byte) 45);
        return result.array();
    }
}
