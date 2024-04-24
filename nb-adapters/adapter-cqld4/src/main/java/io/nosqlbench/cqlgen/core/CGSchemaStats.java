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

package io.nosqlbench.cqlgen.core;

import java.util.HashMap;
import java.util.Map;

public class CGSchemaStats {

    Map<String, CGKeyspaceStats> keyspaces = new HashMap<String, CGKeyspaceStats>();

    public Map<String, CGKeyspaceStats> getKeyspaces() {
        return keyspaces;
    }

    public void setKeyspaces(Map<String, CGKeyspaceStats> keyspaces) {
        this.keyspaces = keyspaces;
    }

    public CGKeyspaceStats getKeyspace(String keyspaceName) {
        return keyspaces.get(keyspaceName);
    }

    public void setKeyspace(CGKeyspaceStats keyspace) {
        this.keyspaces.put(keyspace.getKeyspaceName(), keyspace);
    }

}
