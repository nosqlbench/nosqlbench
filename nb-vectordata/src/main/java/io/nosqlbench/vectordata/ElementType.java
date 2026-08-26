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

import java.util.Locale;

/** Scalar element encodings understood by vectordata payload readers. */
public enum ElementType {
    U8(1, false, false), I8(1, false, true),
    U16(2, false, false), I16(2, false, true),
    U32(4, false, false), I32(4, false, true),
    U64(8, false, false), I64(8, false, true),
    F16(2, true, true), F32(4, true, true), F64(8, true, true);

    private final int width;
    private final boolean floating;
    private final boolean signed;

    ElementType(int width, boolean floating, boolean signed) {
        this.width = width;
        this.floating = floating;
        this.signed = signed;
    }

    public int width() { return width; }
    public boolean floating() { return floating; }
    public boolean signed() { return signed; }

    public static ElementType forExtension(String source) {
        String value = source.toLowerCase(Locale.ROOT);
        int slash = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slash >= 0) value = value.substring(slash + 1);
        int query = value.indexOf('?');
        if (query >= 0) value = value.substring(0, query);
        int dot = value.lastIndexOf('.');
        if (dot < 0) throw new VectorDataException("Vector source has no extension: " + source);
        String extension = value.substring(dot + 1);
        return switch (extension) {
            case "bvec", "bvecs", "u8", "u8vec", "u8vecs", "bvvec", "bvvecs", "u8vvec", "u8vvecs" -> U8;
            case "i8", "i8vec", "i8vecs", "i8vvec", "i8vvecs" -> I8;
            case "u16", "u16vec", "u16vecs", "u16vvec", "u16vvecs" -> U16;
            case "i16", "i16vec", "i16vecs", "svvec", "svvecs", "i16vvec", "i16vvecs" -> I16;
            case "u32", "u32vec", "u32vecs", "u32vvec", "u32vvecs" -> U32;
            case "ivec", "ivecs", "i32", "i32vec", "i32vecs", "ivvec", "ivvecs", "i32vvec", "i32vvecs" -> I32;
            case "u64", "u64vec", "u64vecs", "u64vvec", "u64vvecs" -> U64;
            case "i64", "i64vec", "i64vecs", "i64vvec", "i64vvecs" -> I64;
            case "mvec", "mvecs", "f16", "f16vec", "f16vecs", "mvvec", "mvvecs", "f16vvec", "f16vvecs" -> F16;
            case "fvec", "fvecs", "f32", "f32vec", "f32vecs", "fvvec", "fvvecs", "f32vvec", "f32vvecs" -> F32;
            case "dvec", "dvecs", "f64", "f64vec", "f64vecs", "dvvec", "dvvecs", "f64vvec", "f64vvecs" -> F64;
            default -> throw new VectorDataException("Unsupported vector extension '." + extension + "' in " + source);
        };
    }
}
