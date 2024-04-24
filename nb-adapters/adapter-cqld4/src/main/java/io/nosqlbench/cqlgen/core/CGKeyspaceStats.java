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

public class CGKeyspaceStats {
    String keyspaceName;

    Map<String,String> keyspaceAttributes = new HashMap<String,String>();

    Map<String, CGTableStats> keyspaceTables = new HashMap<String, CGTableStats>();
    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    public Map<String, String> getKeyspaceAttributes() {
        return keyspaceAttributes;
    }

    public String getKeyspaceAttribute(String attributeName) {
        return keyspaceAttributes.get(attributeName);
    }

    public void setKeyspaceAttributes(Map<String, String> keyspaceAttributes) {
        this.keyspaceAttributes = keyspaceAttributes;
    }

    public void setKeyspaceAttribute(String attributeName, String attributeVal) {
        this.keyspaceAttributes.put(attributeName, attributeVal);
    }

    public Map<String, CGTableStats> getKeyspaceTables() {
        return keyspaceTables;
    }

    public CGTableStats getKeyspaceTable(String tableName) {
        return keyspaceTables.get(tableName);
    }

    public void setKeyspaceTables(Map<String, CGTableStats> keyspaceTables) {
        this.keyspaceTables = keyspaceTables;
    }

    public void setKeyspaceTable(String tableName, CGTableStats tableAttributes) {
        this.keyspaceTables.put(tableName, tableAttributes);
    }


}
