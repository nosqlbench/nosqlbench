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

/// A facet was opened with a reader for the other shape. The facet is
/// readable — elsewhere — so the message says where rather than
/// describing the symptom the caller happened to hit.
public class WrongFacetShapeException extends VectorDataException {
    private final String facet;
    private final FacetShape shape;
    private final FacetShape attempted;

    public WrongFacetShapeException(String facet, FacetShape shape, FacetShape attempted) {
        super("facet '" + facet + "' holds " + shape + ", not " + attempted + "; open it with " + shape.reader());
        this.facet = facet; this.shape = shape; this.attempted = attempted;
    }

    /// The facet as declared.
    public String facet() { return facet; }
    /// What the facet actually holds.
    public FacetShape shape() { return shape; }
    /// The shape the caller's reader expects.
    public FacetShape attempted() { return attempted; }
    /// The reader that does open this facet.
    public String reader() { return shape.reader(); }
}
