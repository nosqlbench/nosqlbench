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
/// not a timestamp. Absent means [#BASE], and the dataset is **held to
/// it**: an unversioned manifest is read as what every dataset was
/// before the field existed — single-file facets, one implicit parent,
/// no tag schema — and content that needs more is refused naming the
/// version to declare. A writer emits the lowest version describing
/// what it wrote, so only a change that older readers would misread
/// bumps it: multi-file facets were the first, stated parents and
/// profile tag schemas the second.
public final class FormatVersion {
    private FormatVersion() { }

    /// Everything written before the field existed: single-file facets.
    public static final int BASE = 1;
    /// Multi-file facet series.
    public static final int SHARDED = 2;
    /// Profiles that state their parents, a parent other than `default`
    /// among them, and carry a `profile_tags` schema.
    public static final int TAGGED = 3;
    /// The highest version this implementation reads.
    public static final int SUPPORTED = TAGGED;

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

    /// Refuses a declaration that understates what it holds. A stated
    /// version lower than the content requires is a declaration
    /// contradicting itself, the same class of fault as a record count
    /// that disagrees with its shards. An **absent** field means
    /// [#BASE] and the dataset is held to it: content that needs more
    /// is refused naming the version to declare, so a file that never
    /// said what it is is read as the least it could be rather than the
    /// most a new reader can make of it. A version higher than needed
    /// is merely generous.
    public static void checkStatedAgainstContent(Integer stated, int required) {
        if (stated == null) {
            if (required > BASE)
                throw new VectorDataException("dataset declares no format_version and carries content that requires "
                    + required + "; declare format_version: " + required);
            return;
        }
        if (required > stated)
            throw new VectorDataException("dataset declares format_version " + stated + " but its content requires "
                + required + " — a declaration cannot understate what it holds");
    }
}
