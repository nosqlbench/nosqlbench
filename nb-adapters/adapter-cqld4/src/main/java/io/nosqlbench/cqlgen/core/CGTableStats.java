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

public class CGTableStats {
    String tableName;

    Map<String,String> attributes = new HashMap<String,String>();

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String attributeName, String attributeVal) {
        attributes.put(attributeName, attributeVal);
    }

    public int size() {
        return getAttributes().size();
    }

}
