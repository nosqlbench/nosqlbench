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

import com.datastax.oss.driver.internal.core.util.Strings;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.cqlgen.core.CGKeyspaceStats;

import java.util.*;

public class CqlKeyspaceDef implements NBNamedElement, Labeled {
    String keyspaceName= "";
    CGKeyspaceStats stats;
    private boolean isDurableWrites;
    private String replicationData;
    private final List<CqlTable> tableDefs = new ArrayList<>();
    private final List<CqlType> typeDefs = new ArrayList<>();
    /**
     * Has this been populated by keyspace definition? If false, it is only
     * here because it was vivified by a reference.
     */
    private transient boolean defined;

    public CqlKeyspaceDef() {
    }

    public CqlKeyspaceDef(String ksname) {
        setKeyspaceName(ksname);
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

    public CqlTable getTable(String table) {
        return this.tableDefs.stream().filter(t -> t.getName().equals(table)).findAny().orElse(null);
    }

    public void addTable(CqlTable table) {
        table.setKeyspace(this);
        this.tableDefs.add(table);
    }

    public List<CqlType> getTypeDefs() {
        return this.typeDefs;
    }

    public List<CqlTable> getTableDefs() {
        return this.tableDefs;

    }

    public void removeTable(CqlTable table) {
        this.tableDefs.remove(table.getName());
    }

    public void getReferenceErrors(List<String> errors) {
        if (!defined) {
            errors.add("keyspace " + this.getName() + " was referenced but not defined.");
        }
        for (CqlType typedef : typeDefs) {
            typedef.getReferenceErrors(errors);
        }
        for (CqlTable value : tableDefs) {
            value.getReferenceErrors(errors);
        }
    }

    public void setDefined() {
        if (this.keyspaceName==null) {
            throw new RuntimeException("nuh uh");
        }
        this.defined=true;
    }

    public void validate() {
        Strings.requireNotEmpty(this.keyspaceName, "keyspace name");
    }

    public void addType(CqlType usertype) {
        this.typeDefs.add(usertype);
    }
}
