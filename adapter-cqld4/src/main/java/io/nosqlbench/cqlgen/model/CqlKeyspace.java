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

package io.nosqlbench.cqlgen.model;

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.cqlgen.exporter.CGKeyspaceStats;

import java.util.Map;

public class CqlKeyspace implements NBNamedElement, Labeled {
    String keyspaceName= "";
    CGKeyspaceStats stats;
    private boolean isDurableWrites;
    private String replicationData;

    public CqlKeyspace() {
    }

    public void setKeyspaceName(String newname) {
        this.keyspaceName=newname;
    }

    public String getName() {
        return this.keyspaceName;
    }


    @Override
    public String toString() {
        return "CqlKeyspace{" +
            "keyspaceName='" + keyspaceName + '\'' +
            ", stats=" + stats +
            ", isDurableWrites=" + isDurableWrites +
            ", replicationData='" + replicationData + '\'' +
            '}';
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "name", keyspaceName,
            "type","keyspace"
        );
    }

    public void setStats(CGKeyspaceStats ksstats) {
        this.stats=ksstats;
    }

    public boolean isDurableWrites() {
        return isDurableWrites;
    }

    public void setDurableWrites(boolean isDurableWrites) {
        this.isDurableWrites = isDurableWrites;
    }

    public void setReplicationData(String repldata) {
        this.replicationData = repldata;
    }

    public String getReplicationData() {
        return this.replicationData;
    }
}
