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

import io.nosqlbench.api.labels.Labeled;

import java.util.HashMap;
import java.util.Map;

public class CqlKeyspace implements Labeled {
    String keyspaceName= "";
    String refddl;
    private String refReplDdl;
    Map<String,String> keyspaceAttributes = new HashMap<String,String>();

    public Map<String, String> getKeyspaceAttributes() {
        return keyspaceAttributes;
    }

    public void setKeyspaceAttributes(Map<String, String> keyspaceAttributes) {
        this.keyspaceAttributes = keyspaceAttributes;
    }



    public CqlKeyspace() {
    }

    public void setKeyspaceName(String name) {
        this.keyspaceName=name;
    }

    public String getKeyspaceName() {
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

    public void setRefReplDdl(String refReplDdl) {
        this.refReplDdl=refReplDdl;
    }

    public String getRefDdlWithReplFields(String replFields) {
        return refddl.replace(refReplDdl,replFields);
    }
}
