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

/// The `dataset.yaml` format version: a **minimum reader requirement**,
/// not a timestamp. Absent means [#BASE], which is every dataset in
/// circulation and not a distinct unversioned state. A writer emits the
/// lowest version describing what it wrote, so only a change that older
/// readers would misread — multi-file facets were the first — bumps it.
public final class FormatVersion {
    private FormatVersion() { }

    /// Everything written before the field existed: single-file facets.
    public static final int BASE = 1;
    /// Multi-file facet series.
    public static final int SHARDED = 2;
    /// The highest version this implementation reads.
    public static final int SUPPORTED = SHARDED;

    /// Refuses a dataset this implementation cannot read, naming both
    /// numbers, and returns the effective version otherwise. Shared by
    /// every loader rather than mirrored, so a dataset accepted through
    /// one route is never refused through another.
    public static int checkSupported(Integer stated) {
        int version = stated == null ? BASE : stated;
        if (version > SUPPORTED)
            throw new VectorDataException("dataset requires format_version " + version + "; this build supports up to "
                + SUPPORTED + ". Upgrade nb-vectordata to read it.");
        return version;
    }

    /// Refuses a declaration that understates what it holds: a stated
    /// version lower than the content requires is a declaration
    /// contradicting itself, the same class of fault as a record count
    /// that disagrees with its shards. An absent field is not a claim
    /// and passes; a version higher than needed is merely generous.
    public static void checkStatedAgainstContent(Integer stated, int required) {
        if (stated != null && required > stated)
            throw new VectorDataException("dataset declares format_version " + stated + " but its content requires "
                + required + " — a declaration cannot understate what it holds");
    }
}
