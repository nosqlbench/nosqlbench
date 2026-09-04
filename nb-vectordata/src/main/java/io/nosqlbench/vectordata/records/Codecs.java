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
package io.nosqlbench.vectordata.records;

import io.nosqlbench.vectordata.anode.ANode;
import io.nosqlbench.vectordata.anode.ANodeException;
import io.nosqlbench.vectordata.anode.Vernacular;
import io.nosqlbench.vectordata.anode.Vernaculars;

/// The codecs that compose the two stages:
///
/// ```text
/// slab ──[container]──▶ bytes ──[stage 1]──▶ ANode ──[stage 2]──▶ text ──[tree]──▶ Map
/// ```
///
/// [#ANODE] stops after stage 1; [#text] renders in a vernacular;
/// [#TREE] hands back plain Java values, the role a serde target plays
/// in the reference. There is one read path: the untyped level is not
/// a separate implementation but the codec that stops early, and a
/// codec resolved [#byName] is the same object a typed call would use.
public final class Codecs {
    private Codecs() { }

    /// Stage 1 only: the record as the node it says it is. The dialect
    /// comes from the record's leading byte, so a facet holding a mix
    /// of MNodes and PNodes reads without the caller declaring which
    /// is which.
    public static final RecordCodec<ANode> ANODE = bytes -> {
        try { return ANode.decode(bytes); }
        catch (ANodeException e) { throw RecordException.decode(e.getMessage()); }
    };

    /// Stages 1 and 2: the record rendered in a vernacular. The output
    /// is text by construction — a vernacular is a rendering, not a
    /// type. A caller wanting structure wants [#TREE].
    public static RecordCodec<String> text(Vernacular vernacular) {
        return bytes -> Vernaculars.render(ANODE.decode(bytes), vernacular);
    }

    /// Stages 1, 2 and the tree: the record as plain Java values —
    /// an insertion-ordered map of `String`, `Long`, `Double`,
    /// `Boolean`, `null`, lists and nested maps, typed as the JSON
    /// vernacular types them.
    public static final RecordCodec<Object> TREE = bytes -> Vernaculars.toTree(ANODE.decode(bytes));

    /// The text codec a vernacular name selects, or `null` for a name
    /// that is none. Resolved by the same [Vernacular#parse] every other
    /// by-name surface uses, so a codec selected at runtime and one
    /// written in code reach identical decoding. `anode` names no
    /// vernacular, because it produces no text.
    public static RecordCodec<String> byName(String name) {
        Vernacular vernacular = Vernacular.parse(name);
        return vernacular == null ? null : text(vernacular);
    }
}
