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

import io.nosqlbench.vectordata.VectorDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** Parsed vectordata `.mref` footer and leaf hashes. */
public final class MerkleReference {
    private final byte[][] leaves;
    private final long chunkSize;
    private final long contentSize;

    private MerkleReference(byte[][] leaves, long chunkSize, long contentSize) {
        this.leaves = leaves;
        this.chunkSize = chunkSize;
        this.contentSize = contentSize;
    }
    public long chunkSize() { return chunkSize; }
    public long contentSize() { return contentSize; }
    public int leafCount() { return leaves.length; }
    public byte[] leaf(int index) { return leaves[index].clone(); }

    public static MerkleReference parse(byte[] raw, String source) {
        if (raw.length < 41) throw new VectorDataException("Malformed .mref for " + source + ": too short");
        int footerLength = raw[raw.length - 1] & 0xff;
        if (footerLength != 41 && footerLength != 45)
            throw new VectorDataException("Malformed .mref for " + source + ": unsupported footer length " + footerLength);
        if (raw.length < footerLength) throw new VectorDataException("Malformed .mref for " + source);
        int footerOffset = raw.length - footerLength;
        ByteBuffer footer = ByteBuffer.wrap(raw, footerOffset, footerLength).order(ByteOrder.BIG_ENDIAN);
        long chunkSize = footer.getLong();
        long contentSize = footer.getLong();
        int totalChunks = footer.getInt();
        int leafCount = footer.getInt();
        int capacityLeaves = footer.getInt();
        int nodeCount = footer.getInt();
        footer.getInt(); // root offset
        int internalNodeCount = footer.getInt();
        if (footerLength == 45) footer.getInt(); // valid-bitset size
        if (chunkSize <= 0 || contentSize < 0 || totalChunks < 0 || leafCount < totalChunks || capacityLeaves < leafCount || nodeCount < leafCount)
            throw new VectorDataException("Malformed .mref footer for " + source);
        long hashBytes = (long) nodeCount * 32L;
        if (hashBytes != footerOffset) throw new VectorDataException("Malformed .mref node section for " + source);
        long expectedChunks = contentSize == 0 ? 0 : (contentSize + chunkSize - 1) / chunkSize;
        if (expectedChunks != totalChunks) throw new VectorDataException("Malformed .mref chunk count for " + source);
        if (internalNodeCount < 0 || internalNodeCount + leafCount != nodeCount)
            throw new VectorDataException("Malformed .mref tree shape for " + source);
        byte[][] leaves = new byte[leafCount][];
        // vectordata stores leaves after all internal nodes. Padded leaves protect no content.
        for (int i = 0; i < totalChunks; i++) leaves[i] = Arrays.copyOfRange(raw, (internalNodeCount + i) * 32, (internalNodeCount + i + 1) * 32);
        return new MerkleReference(Arrays.copyOf(leaves, totalChunks), chunkSize, contentSize);
    }
}
