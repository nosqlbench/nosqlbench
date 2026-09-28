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

import io.nosqlbench.vectordata.VectorDataException;

/// What went wrong reading or decoding a record. The [Kind] is
/// matchable, not only readable, and each carries what it concerns.
public class RecordException extends VectorDataException {
    public enum Kind {
        /// The facet holds element runs, not opaque records — the caller
        /// is at the wrong door, and the message names the right one.
        WRONG_SHAPE,
        /// The facet declares no readable container.
        NOT_A_CONTAINER,
        /// The container could not be opened or read.
        CONTAINER,
        /// The ordinal lies outside the facet.
        OUT_OF_BOUNDS,
        /// The record's bytes could not be decoded by the chosen codec.
        DECODE
    }

    private final Kind kind;
    private final String facet;
    private final String reader;
    private final long ordinal;

    private RecordException(Kind kind, String message, String facet, String reader, long ordinal) {
        super(message);
        this.kind = kind; this.facet = facet; this.reader = reader; this.ordinal = ordinal;
    }

    public Kind kind() { return kind; }
    /// The facet concerned, for `WRONG_SHAPE`.
    public String facet() { return facet; }
    /// The reader that does open the facet, for `WRONG_SHAPE`.
    public String reader() { return reader; }
    /// The ordinal concerned, for `OUT_OF_BOUNDS`.
    public long ordinal() { return ordinal; }

    public static RecordException wrongShape(String facet, String reader) {
        return new RecordException(Kind.WRONG_SHAPE, "facet '" + facet + "' holds element runs, not opaque records; open it with " + reader, facet, reader, -1);
    }
    public static RecordException notAContainer(String what) { return new RecordException(Kind.NOT_A_CONTAINER, "not a record container: " + what, null, null, -1); }
    public static RecordException container(String message) { return new RecordException(Kind.CONTAINER, message, null, null, -1); }
    public static RecordException outOfBounds(long ordinal) { return new RecordException(Kind.OUT_OF_BOUNDS, "ordinal " + ordinal + " is past the end of this facet", null, null, ordinal); }
    public static RecordException decode(String detail) { return new RecordException(Kind.DECODE, "decode: " + detail, null, null, -1); }
}
