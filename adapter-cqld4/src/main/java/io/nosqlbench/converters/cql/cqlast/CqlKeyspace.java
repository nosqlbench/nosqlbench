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

public class CqlKeyspace {
    String keyspaceName= "";
    String refddl;

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
}
