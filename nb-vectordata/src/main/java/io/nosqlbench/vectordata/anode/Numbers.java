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

import java.math.BigDecimal;

/// Number spellings that match the reference's `Display` output, so a
/// record rendered here reads byte-for-byte as it does there: the
/// shortest digits that round-trip, always positional (never an
/// exponent), and no fractional part when the value is whole —
/// `1.0` prints as `1`, `99.5` as `99.5`, `1e21` as its twenty-two
/// digits. Renderings that need a decimal point add it themselves.
final class Numbers {
    private Numbers() { }

    /// A double as the reference prints an `f64`.
    static String f64(double value) { return shortest(Double.toString(value), value); }

    /// A float as the reference prints an `f32`: the shortest digits
    /// that round-trip at single precision, which is why it is not
    /// `f64((double) value)`.
    static String f32(float value) { return shortest(Float.toString(value), value); }

    private static String shortest(String javaShortest, double value) {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return value > 0 ? "inf" : "-inf";
        if (value == 0) return 1 / value < 0 ? "-0" : "0";
        String plain = new BigDecimal(javaShortest).stripTrailingZeros().toPlainString();
        return plain;
    }

    /// A number in a spelling that carries its float-ness: the
    /// reference appends `.0` when the shortest form has no point.
    static String withPoint(String rendered) { return rendered.contains(".") || rendered.contains("N") || rendered.contains("inf") ? rendered : rendered + ".0"; }

    /// Lowercase hex, two digits per byte.
    static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) text.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
        return text.toString();
    }

    /// A 16-byte identifier in the hyphenated `8-4-4-4-12` form.
    static String uuid(byte[] bytes) {
        String hex = hex(bytes);
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20, 32);
    }
}
