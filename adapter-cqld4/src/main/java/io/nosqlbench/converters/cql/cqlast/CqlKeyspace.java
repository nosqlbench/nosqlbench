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

package io.nosqlbench.converters.cql.cqlast;

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.converters.cql.exporters.CGKeyspaceStats;

import java.util.Map;

public class CqlKeyspace implements NBNamedElement, Labeled {
    String keyspaceName= "";
    String refddl;
    private String refReplDdl;
    CGKeyspaceStats stats;

    public CqlKeyspace() {
    }

    public void setKeyspaceName(String newname) {
        if (this.refddl!=null) {
            this.refddl = refddl.replaceAll(this.keyspaceName, newname);
        }
        this.keyspaceName=newname;
    }

    public String getName() {
        return this.keyspaceName;
    }

    public void setRefDdl(String refddl) {
        this.refddl = refddl;
    }

    @Override
    public String toString() {
        return "CqlKeyspace{" +
            "keyspaceName='" + keyspaceName + '\'' +
            ", refddl='" + refddl + '\'' +
            '}';
    }

    public String getRefddl() {
        return refddl;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", keyspaceName
        );
    }

    public void setRefReplDdl(String newRefReplDdl) {
        if (this.refddl!=null) {
            this.refddl=this.refddl.replaceAll(this.refReplDdl,newRefReplDdl);
        }
        this.refReplDdl=newRefReplDdl;
    }

    public String getRefDdlWithReplFields(String replFields) {
        refddl.replace(refReplDdl,replFields);
        return refddl;
    }

    public String getReplRefDdl() {
        return this.refReplDdl;
    }

    public void setStats(CGKeyspaceStats ksstats) {
        this.stats=ksstats;
    }
}
