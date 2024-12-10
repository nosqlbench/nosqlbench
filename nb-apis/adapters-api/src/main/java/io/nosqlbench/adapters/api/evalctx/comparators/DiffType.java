package io.nosqlbench.adapters.api.evalctx.comparators;

/*
 * Copyright (c) nosqlbench
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

    /// Verify nothing for this result
    none(0),

    /// Verify that keys named in the result are present in the reference map.
    result_fields(0x1),

    /// Verify that keys in the reference map are present in the result map.
    reference_fields(0x1 << 1),

    /// Verify that all fields present in either the reference map or the result map
    /// are also present in the other. (set equality)
    fields(0x1 | 0x1 << 1),

    /// Verify that all values of the same named key are equal, according to
    /// {@link Object#equals(Object)}}.
    values(0x1 << 2),

    /// Cross-verify all names and values between the reference map and result map.
    all(0x1 | 0x1 << 1 | 0x1 << 2);

    public int bitmask;

    DiffType(int bit) {
        this.bitmask = bit;
    }

    public boolean is(DiffType option) {
        return (bitmask & option.bitmask) > 0;
    }

}
