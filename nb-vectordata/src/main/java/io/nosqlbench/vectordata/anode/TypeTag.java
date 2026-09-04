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

/// Type tags for MNode field values: the single-byte discriminant that
/// precedes a value on the wire. Assignments are stable and shared with
/// the `veks-anode` reference; a byte outside `0..28` is not a tag.
public enum TypeTag {
    TEXT(0, "text"), INT(1, "int"), FLOAT(2, "float"), BOOL(3, "bool"), BYTES(4, "bytes"), NULL(5, "null"),
    ENUM_STR(6, "enum_str"), ENUM_ORD(7, "enum_ord"), LIST(8, "list"), MAP(9, "map"), TEXT_VALIDATED(10, "text_validated"),
    ASCII(11, "ascii"), INT32(12, "int32"), SHORT(13, "short"), DECIMAL(14, "decimal"), VARINT(15, "varint"),
    FLOAT32(16, "float32"), HALF(17, "half"), MILLIS(18, "millis"), NANOS(19, "nanos"), DATE(20, "date"), TIME(21, "time"),
    DATETIME(22, "datetime"), UUID_V1(23, "uuid_v1"), UUID_V7(24, "uuid_v7"), ULID(25, "ulid"), ARRAY(26, "array"),
    SET(27, "set"), TYPED_MAP(28, "typed_map");

    private static final TypeTag[] BY_CODE = values();
    private final int code;
    private final String label;

    TypeTag(int code, String label) { this.code = code; this.label = label; }

    /// The wire byte.
    public int code() { return code; }

    /// The human-readable name (`text`, `int32`, `typed_map`, …).
    public String label() { return label; }

    /// The tag for a wire byte, or `null` for a byte that names none.
    public static TypeTag fromCode(int code) { return code >= 0 && code < BY_CODE.length ? BY_CODE[code] : null; }

    @Override public String toString() { return label; }
}
