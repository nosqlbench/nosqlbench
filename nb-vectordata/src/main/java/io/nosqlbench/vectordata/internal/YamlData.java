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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.VectorDataException;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.schema.CoreSchema;

import java.util.LinkedHashMap;
import java.util.Map;

/// Checked conversion helpers for permissive YAML object trees.
///
/// Documents are read under the YAML 1.2 **core** schema, the dialect
/// the reference writes and reads: `~`, `null`, and `Null` are null —
/// a naming tag in a `profile_tags` schema is declared as `~` — and
/// `0x10` is an integer, while `yes` and `1_000` stay strings. The
/// engine's default JSON schema would read `~` as the string it spells.
public final class YamlData {
    private YamlData() { }
    public static Map<String, Object> map(Object value, String label) {
        if (!(value instanceof Map<?, ?> raw)) throw new VectorDataException(label + " must be a YAML mapping");
        Map<String, Object> result = new LinkedHashMap<>(); raw.forEach((key, item) -> result.put(String.valueOf(key), item)); return result;
    }
    public static Object parseValue(String text, String label) { return new Load(LoadSettings.builder().setLabel(label).setSchema(new CoreSchema()).build()).loadFromString(text); }
    public static Map<String, Object> parse(String text, String label) { return map(parseValue(text, label), label); }
    public static String string(Object value, String label) { if (value == null) throw new VectorDataException("Missing " + label); return String.valueOf(value); }
    public static String optionalString(Object value) { return value == null ? null : String.valueOf(value); }
    public static int integer(Object value, String label) {
        if (value instanceof Number number) return number.intValue();
        try { return Integer.parseInt(string(value, label)); } catch (NumberFormatException e) { throw new VectorDataException("Invalid integer " + label, e); }
    }
}
