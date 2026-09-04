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

import java.util.List;

/// Family accessors over [MValue]: the string a text-family value
/// carries, the items a container holds, the sixteen bytes an
/// identifier holds. Renderers switch on the tag and reach the payload
/// through these rather than casting at every site.
final class MValues {
    private MValues() { }

    /// The string of a `Text`, `EnumStr`, `Ascii`, `Date`, `Time`, or
    /// `DateTime`; `null` for any other value.
    static String text(MValue v) {
        if (v instanceof MValue.Text x) return x.value();
        if (v instanceof MValue.EnumStr x) return x.value();
        if (v instanceof MValue.Ascii x) return x.value();
        if (v instanceof MValue.Date x) return x.value();
        if (v instanceof MValue.Time x) return x.value();
        if (v instanceof MValue.DateTime x) return x.value();
        return null;
    }

    /// The items of a `List`, `Set`, or `Array`; `null` otherwise.
    static List<MValue> items(MValue v) {
        if (v instanceof MValue.ListValue x) return x.items();
        if (v instanceof MValue.SetValue x) return x.items();
        if (v instanceof MValue.ArrayValue x) return x.items();
        return null;
    }

    /// The sixteen bytes of a `UuidV1`, `UuidV7`, or `Ulid`; `null` otherwise.
    static byte[] identifier(MValue v) {
        if (v instanceof MValue.UuidV1 x) return x.value();
        if (v instanceof MValue.UuidV7 x) return x.value();
        if (v instanceof MValue.Ulid x) return x.value();
        return null;
    }

    /// The integer of an `Int`, `Millis`, `Int32`, `EnumOrd`, `Short`,
    /// or `Half` (its bits); `null` otherwise.
    static Long integer(MValue v) {
        if (v instanceof MValue.Int x) return x.value();
        if (v instanceof MValue.Millis x) return x.value();
        if (v instanceof MValue.Int32 x) return (long) x.value();
        if (v instanceof MValue.EnumOrd x) return (long) x.value();
        if (v instanceof MValue.Short x) return (long) x.value();
        if (v instanceof MValue.Half x) return (long) x.bits();
        return null;
    }
}
