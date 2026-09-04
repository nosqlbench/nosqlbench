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

/// What went wrong walking raw MNode bytes. The [Kind] is matchable,
/// not only readable.
public class ScanException extends ANodeException {
    public enum Kind {
        /// The data ended before the structure it declares was complete.
        UNEXPECTED_EOF,
        /// A type tag byte the wire format does not define.
        INVALID_TAG,
        /// The record does not begin with the MNode dialect leader.
        INVALID_DIALECT
    }

    private final Kind kind;
    private final int value;

    private ScanException(Kind kind, String message, int value) { super(message); this.kind = kind; this.value = value; }

    public Kind kind() { return kind; }
    /// The offending tag or dialect byte, for `INVALID_TAG` and `INVALID_DIALECT`.
    public int value() { return value; }

    public static ScanException unexpectedEof() { return new ScanException(Kind.UNEXPECTED_EOF, "unexpected end of data", -1); }
    public static ScanException invalidTag(int tag) { return new ScanException(Kind.INVALID_TAG, "invalid type tag: " + tag, tag); }
    public static ScanException invalidDialect(int dialect) { return new ScanException(Kind.INVALID_DIALECT, String.format("invalid dialect: 0x%02x", dialect), dialect); }
}
