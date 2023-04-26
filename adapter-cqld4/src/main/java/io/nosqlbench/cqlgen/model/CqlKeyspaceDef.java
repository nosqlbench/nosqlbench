/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.cqlgen.core.CGKeyspaceStats;

import java.util.*;

public class CqlKeyspaceDef implements NBNamedElement, NBLabeledElement {
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
    private boolean defined;

    public CqlKeyspaceDef() {
    }

    public CqlKeyspaceDef(final String ksname) {
        keyspaceName = ksname;
    }

    public void setKeyspaceName(final String newname) {
        keyspaceName=newname;
    }

    @Override
    public String getName() {
        return keyspaceName;
    }

    @Override
    public String toString() {
        return "CqlKeyspace{" +
            "keyspaceName='" + this.keyspaceName + '\'' +
            ", stats=" + this.stats +
            ", isDurableWrites=" + this.isDurableWrites +
            ", replicationData='" + this.replicationData + '\'' +
            '}';
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "name", this.keyspaceName,
            "type","keyspace"
        );
    }

    public void setStats(final CGKeyspaceStats ksstats) {
        stats=ksstats;
    }

    public boolean isDurableWrites() {
        return this.isDurableWrites;
    }

    public void setDurableWrites(final boolean isDurableWrites) {
        this.isDurableWrites = isDurableWrites;
    }

    public void setReplicationData(final String repldata) {
        replicationData = repldata;
    }

    public String getReplicationData() {
        return replicationData;
    }

    public CqlTable getTable(final String table) {
        return tableDefs.stream().filter(t -> t.getName().equals(table)).findAny().orElse(null);
    }

    public void addTable(final CqlTable table) {
        table.setKeyspace(this);
        tableDefs.add(table);
    }

    public List<CqlType> getTypeDefs() {
        return typeDefs;
    }

    public List<CqlTable> getTableDefs() {
        return tableDefs;

    }

    public void removeTable(final CqlTable table) {
        tableDefs.remove(table.getName());
    }

    public void getReferenceErrors(final List<String> errors) {
        if (!this.defined) errors.add("keyspace " + keyspaceName + " was referenced but not defined.");
        for (final CqlType typedef : this.typeDefs) typedef.getReferenceErrors(errors);
        for (final CqlTable value : this.tableDefs) value.getReferenceErrors(errors);
    }

    public void setDefined() {
        if (null == this.keyspaceName) throw new RuntimeException("nuh uh");
        defined=true;
    }

    public void validate() {
        Strings.requireNotEmpty(keyspaceName, "keyspace name");
    }

    public void addType(final CqlType usertype) {
        typeDefs.add(usertype);
    }
}
