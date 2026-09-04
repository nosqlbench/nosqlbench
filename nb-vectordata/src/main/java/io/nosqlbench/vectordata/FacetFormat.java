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

import java.util.List;
import java.util.Locale;

/// The file formats the facet spec names, by extension, and the
/// [FacetShape] each holds.
public enum FacetFormat {
    /// Float xvec — vectors and distances.
    FLOAT_XVEC(FacetShape.ELEMENTS, "fvecs", "dvecs", "mvecs", "fvec", "dvec", "mvec", "f32vecs", "f64vecs", "f16vecs", "f32vec", "f64vec", "f16vec"),
    /// Fixed-width integer xvec — neighbor ids.
    INTEGER_XVEC(FacetShape.ELEMENTS, "ivecs", "i32vecs", "u32vecs", "ivec", "i32vec", "u32vec", "i8vecs", "u8vecs", "bvecs", "i16vecs", "u16vecs", "svecs",
        "i8vec", "u8vec", "bvec", "i16vec", "u16vec", "svec", "i64vecs", "u64vecs", "i64vec", "u64vec"),
    /// Variable-length integer xvec — per-query predicate matches.
    INTEGER_VAR_XVEC(FacetShape.ELEMENTS, "ivvecs", "i32vvecs", "u32vvecs", "ivvec", "i32vvec", "u32vvec"),
    /// Raw packed scalars — flat metadata columns.
    SCALAR_PACKED(FacetShape.ELEMENTS, "u8", "i8", "u16", "i16", "u32", "i32", "u64", "i64"),
    /// A slab — MNode/PNode records and layout namespaces.
    SLAB(FacetShape.RECORDS, "slab");

    private final FacetShape shape;
    private final List<String> extensions;

    FacetFormat(FacetShape shape, String... extensions) { this.shape = shape; this.extensions = List.of(extensions); }

    public FacetShape shape() { return shape; }
    public List<String> extensions() { return extensions; }

    /// The format an extension belongs to, or `null` when the spec names
    /// none for it.
    public static FacetFormat fromExtension(String extension) {
        String wanted = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        for (FacetFormat format : values()) if (format.extensions.contains(wanted)) return format;
        return null;
    }
}
