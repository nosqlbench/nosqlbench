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

/// Backing mode selected for a source. Ranked by what a caller must plan
/// around — a full transfer constrains most, a local file least, and the
/// two chunked modes differ in trust rather than access — so that a facet
/// spanning several files can report the [#weakest] of them.
public enum AccessMode {
    LOCAL(3), MERKLE_HASHED(2), MERKLE_CHUNKED(1), FULL_TRANSFER(0);

    private final int strength;
    AccessMode(int strength) { this.strength = strength; }

    /// The weakest of several modes — the one true of every file. A
    /// facet's mode is a promise about every read it will serve, and a
    /// caller that plans against "supports range" and then reaches a
    /// file that does not has been misled by an average. Understating
    /// the good files costs efficiency; overstating costs correctness.
    /// `null` for no modes: making no promise is not promising little.
    public static AccessMode weakest(Iterable<AccessMode> modes) {
        AccessMode result = null;
        for (AccessMode mode : modes) if (result == null || mode.strength < result.strength) result = mode;
        return result;
    }
}
