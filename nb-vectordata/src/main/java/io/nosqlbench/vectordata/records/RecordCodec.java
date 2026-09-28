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

/// Turns a record's bytes into a value.
///
/// A codec is a **value**, not just a type, so one chosen at runtime —
/// a vernacular named in a setting — is the same kind of thing as one
/// written in code, and both reach the same [#decode]. The built-in
/// codecs live in [Codecs]; a caller wanting some other decoding
/// implements this directly.
@FunctionalInterface
public interface RecordCodec<T> {
    /// Decodes one record. A record the codec cannot read fails with a
    /// [RecordException] of kind `DECODE`.
    T decode(byte[] bytes);
}
