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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// What a [ProfileSelector] is evaluated against: one profile's
/// automatic `profile` tag (its name), its structural fields, and its
/// declared `attributes:`.
///
/// Built from a profile **as loaded**, after inheritance, so an
/// inherited `base_count` or `maxk` is as selectable as a declared one;
/// attributes are never inherited, so the map is the profile's own.
/// `inherits` is the parent as **stated**, resolved or not, and `null`
/// when the profile states none.
public record ProfileFacts(String name, Long baseCount, Integer maxk, boolean partition, String inherits,
                           Map<String, Object> attributes) {

    /// The keys a selector reads before the attribute map.
    public static final List<String> STRUCTURAL_KEYS = List.of("profile", "base_count", "maxk", "partition", "inherits");

    public ProfileFacts {
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes == null ? Map.of() : attributes));
    }

    /// One line naming the profile and everything a selector can read
    /// of it, for a message that shows what was on offer.
    public String summary() {
        List<String> parts = new ArrayList<>();
        if (baseCount != null) parts.add("base_count=" + baseCount);
        if (maxk != null) parts.add("maxk=" + maxk);
        if (partition) parts.add("partition=true");
        if (inherits != null) parts.add("inherits=" + inherits);
        for (Map.Entry<String, Object> attribute : attributes.entrySet())
            parts.add(attribute.getKey() + "=" + render(attribute.getValue()));
        return parts.isEmpty() ? name : name + " (" + String.join(", ", parts) + ")";
    }

    /// The canonical text of a scalar attribute value, as the reference
    /// renders one: a string as written, a number as YAML would
    /// serialize it — an integral float without its point, no exponent
    /// — and a boolean as `true`/`false`. `null` for a list, a map, or
    /// a null.
    public static String canonicalText(Object value) {
        if (value instanceof String text) return text;
        if (value instanceof Boolean flag) return flag.toString();
        if (value instanceof Double || value instanceof Float)
            return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros().toPlainString();
        if (value instanceof Number number) return number.toString();
        return null;
    }

    private static String render(Object value) {
        if (value == null) return "~";
        String scalar = canonicalText(value);
        if (scalar != null) return scalar;
        if (value instanceof List<?> list) {
            List<String> items = new ArrayList<>();
            for (Object item : list) items.add(render(item));
            return "[" + String.join(", ", items) + "]";
        }
        if (value instanceof Map<?, ?> map) {
            List<String> items = new ArrayList<>();
            map.forEach((key, item) -> items.add(key + ": " + render(item)));
            return "{" + String.join(", ", items) + "}";
        }
        return String.valueOf(value);
    }
}
