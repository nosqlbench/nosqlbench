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
package io.nosqlbench.vectordata.anode;

/// Any node: an [MNode] record or a [PNode] predicate tree, and the
/// stage-1 codec that tells them apart by the dialect leader byte —
/// `0x01` for an MNode, `0x02` for a PNode, `0x00` never valid. A
/// record says what it is; a reader never infers the dialect from the
/// facet it came from.
///
/// ```text
/// bytes ←→ [stage 1: this] ←→ ANode ←→ [stage 2: Vernaculars] ←→ text
/// ```
public sealed interface ANode {
    int DIALECT_INVALID = 0x00;
    int DIALECT_MNODE = MNode.DIALECT;
    int DIALECT_PNODE = PNode.DIALECT;

    record M(MNode node) implements ANode { }
    record P(PNode node) implements ANode { }

    static ANode of(MNode node) { return new M(node); }
    static ANode of(PNode node) { return new P(node); }

    /// The node's fingerprint, by its own kind.
    default ANode fingerprint() {
        if (this instanceof M m) return new M(m.node().fingerprint());
        return new P(((P) this).node().fingerprint());
    }

    /// Whether two nodes are of the same kind and congruent.
    default boolean isCongruent(ANode other) {
        if (this instanceof M a && other instanceof M b) return a.node().isCongruent(b.node());
        if (this instanceof P a && other instanceof P b) return a.node().isCongruent(b.node());
        return false;
    }

    /// The reference's `Display` of the inner node.
    default String display() { return this instanceof M m ? m.node().display() : ((P) this).node().display(); }

    /// Decodes bytes by their leader byte.
    static ANode decode(byte[] data) {
        if (data.length == 0) throw new ANodeException("empty data");
        return switch (data[0] & 0xff) {
            case DIALECT_MNODE -> decodeMNode(data);
            case DIALECT_PNODE -> decodePNode(data);
            default -> throw new ANodeException(String.format("unknown dialect leader byte: 0x%02x", data[0] & 0xff));
        };
    }

    /// Decodes as an MNode; the bytes must still carry the leader.
    static ANode decodeMNode(byte[] data) {
        try { return new M(MNode.fromBytes(data)); }
        catch (ANodeException e) { throw new ANodeException("MNode decode error: " + e.getMessage()); }
    }

    /// Decodes as a named-mode PNode; the bytes must still carry the leader.
    static ANode decodePNode(byte[] data) {
        try { return new P(PNode.fromBytesNamed(data)); }
        catch (ANodeException e) { throw new ANodeException("PNode decode error: " + e.getMessage()); }
    }

    /// Encodes with the leader byte — an MNode as itself, a PNode in
    /// the typed named mode.
    static byte[] encode(ANode node) { return node instanceof M m ? m.node().toBytes() : ((P) node).node().toBytesNamed(); }
}
