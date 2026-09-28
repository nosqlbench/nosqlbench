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

import java.util.Locale;

/// A human-readable rendering of an [ANode] — stage 2 of the codec.
/// Each vernacular knows how to render both an MNode and a PNode in its
/// own syntax; the ones marked parseable read their own output back.
///
/// | Name | MNode | PNode | Parseable |
/// |------|-------|-------|-----------|
/// | `json` | pretty object | tree | yes |
/// | `jsonl` | one-line object | compact tree | yes |
/// | `yaml` | mapping | tree | yes |
/// | `sql`, `sqlite` | `(values…)` | `WHERE` expression | yes |
/// | `cql` | `(values…)` | `WHERE` expression | yes |
/// | `cddl` | `{ field : type }` | predicate group | yes |
/// | `readout` | colon-aligned lines | indented tree | yes |
/// | `sql-schema`, `sqlite-schema`, `cql-schema` | column DDL | — | no |
/// | `cddl-value` | `{ field : value }` | — | no |
/// | `display` | the `Display` form | the `Display` form | no |
public enum Vernacular {
    CDDL("cddl"), CDDL_VALUE("cddl-value", "cddlvalue"), SQL("sql"), SQL_SCHEMA("sql-schema", "sqlschema"),
    SQLITE("sqlite"), SQLITE_SCHEMA("sqlite-schema", "sqliteschema"), CQL("cql"), CQL_SCHEMA("cql-schema", "cqlschema"),
    JSON("json"), JSONL("jsonl"), YAML("yaml"), READOUT("readout"), DISPLAY("display");

    private final String[] names;
    Vernacular(String... names) { this.names = names; }

    /// The canonical name, as a setting names it.
    public String label() { return names[0]; }

    /// The vernacular a name selects, case-insensitively, or `null`.
    public static Vernacular parse(String name) {
        String wanted = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        for (Vernacular v : values()) for (String n : v.names) if (n.equals(wanted)) return v;
        return null;
    }

    @Override public String toString() { return names[0]; }
}
