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

/// How a facet's records are addressed, and therefore which reader
/// opens it. Some facets hold runs of a fixed-width element and some
/// hold opaque records of their own length. That is a fact about the
/// data, not a gap in the reader, and it is exposed rather than
/// smoothed over: a caller asks which shape a facet has and takes the
/// matching path, instead of trying one and reading an error.
public enum FacetShape {
    /// Records are runs of a fixed-width element, addressed as vectors:
    /// [TestDataView#openFacet], [TestDataView#baseVectors] and kin.
    ELEMENTS("openFacet() / baseVectors()"),
    /// Records are opaque, of their own length, addressed by ordinal:
    /// [TestDataView#openFacetRecords].
    RECORDS("openFacetRecords()");

    private final String reader;
    FacetShape(String reader) { this.reader = reader; }

    /// The reader that opens a facet of this shape.
    public String reader() { return reader; }

    @Override public String toString() { return this == ELEMENTS ? "element runs" : "opaque records"; }
}
