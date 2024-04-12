/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.cqlgen.transformers;

public class ComputedTableStats {
    private double readShareOfTotalOps;
    private double readShareOfTotalReads;
    private double writeShareOfTotalOps;
    private double writeShareOfTotalWrites;
    private double opShareOfTotalOps;
    private double spaceUsedOfTotalSpace;

    public ComputedTableStats setReadShareOfTotalOps(double v) {
        this.readShareOfTotalOps = v;
        return this;
    }

    public ComputedTableStats setReadShareOfTotalReads(double v) {
        this.readShareOfTotalReads =v;
        return this;
    }

    public ComputedTableStats setWriteShareOfTotalOps(double v) {
        this.writeShareOfTotalOps = v;
        return this;
    }

    public ComputedTableStats setWriteShareOfTotalWrites(double v) {
        this.writeShareOfTotalWrites = v;
        return this;
    }

    public ComputedTableStats setOpShareOfTotalOps(double op_share_total) {
        this.opShareOfTotalOps = op_share_total;
        return this;
    }

    public ComputedTableStats setSpaceUsedOfTotalSpace(double weightedSpace) {
        this.spaceUsedOfTotalSpace = weightedSpace;
        return this;
    }

    public double getOpShareOfTotalOps() {
        return opShareOfTotalOps;
    }

    public double getWeightedReadsOfTotal() {
        return this.readShareOfTotalOps;
    }

    public double getWeightedWritesOfTotal() {
        return this.writeShareOfTotalWrites;
    }
}
