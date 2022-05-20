package io.nosqlbench.virtdata.api.annotations;

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


public enum Range {
    NonNegativeLongs("All positive long values and zero: 0L.." + Long.MAX_VALUE),
    NonNegativeInts("All positive integer values and zero: 0.." + Integer.MAX_VALUE),
    Longs("All long values: " + Long.MIN_VALUE + "L.." + Long.MAX_VALUE+"L"),
    Integers("All int values: " + Integer.MIN_VALUE + ".." + Integer.MAX_VALUE),
    DoubleUnitInterval("The unit interval in double precision: 0.0D..1.0D"),
    FloatUnitInterval("The unit interval in single precision: 0.0F..1.0F"),
    Doubles("All double values: " + Double.MIN_VALUE + "D.." + Double.MAX_VALUE+"D");

    private final String description;

    public String getDescription() {
        return description;
    }

    Range(String description) {
        this.description = description;
    }
}
