package io.nosqlbench.activitytype.cql.statements.rowoperators.verification;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


public enum DiffType {

    /// Verify nothing for this statement
    none(0),

    /// Verify that fields named in the row are present in the reference map.
    rowfields(0x1),

    /// Verify that fields in the reference map are present in the row data.
    reffields(0x1 << 1),

    /// Verify that all fields present in either the row or the reference data
    /// are also present in the other.
    fields(0x1 | 0x1 << 1),

    /// Verify that all values of the same named field are equal, according to
    /// {@link Object#equals(Object)}}.
    values(0x1<<2),

    /// Cross-verify all fields and field values between the reference data and
    /// the actual data.
    all(0x1|0x1<<1|0x1<<2);

    public final int bitmask;

    DiffType(int bit) {
        this.bitmask = bit;
    }

    public boolean is(DiffType option) {
        return (bitmask & option.bitmask) > 0;
    }

}
