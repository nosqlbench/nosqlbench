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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.*;

public class StatementsOwner extends RawStmtFields {

    private final static List<String> stmtsFieldNames = List.of("op","ops","operation","statement","statements");

    private List<RawStmtDef> rawStmtDefs = new ArrayList<>();

    public StatementsOwner() {
    }

    public List<RawStmtDef> getRawStmtDefs() {
        return rawStmtDefs;
    }

    public void setRawStmtDefs(List<RawStmtDef> rawStmtDefs) {
        this.rawStmtDefs = rawStmtDefs;
    }

    public void setFieldsByReflection(Map<String, Object> propsmap) {

        HashSet<String> found = new HashSet<>();
        for (String fname : stmtsFieldNames) {
            if (propsmap.containsKey(fname)) {
                found.add(fname);
            }
        }
        if (found.size()>1) {
            throw new BasicError("You used " + found + " as an op name, but only one of these is allowed.");
        }
        if (found.size()==1) {
            Object stmtsFieldValue = propsmap.remove(found.iterator().next());
            setStatementsFieldByType(stmtsFieldValue);
        }

        super.setFieldsByReflection(propsmap);
    }

    @SuppressWarnings("unchecked")
    public void setStatementsFieldByType(Object object) {
        if (object instanceof List) {
            List<Object> stmtList = (List<Object>) object;
            List<RawStmtDef> defs = new ArrayList<>(stmtList.size());
            for (int i = 0; i < stmtList.size(); i++) {
                String defaultName = "stmt" + (i + 1);
                Object o = stmtList.get(i);
                if (o instanceof String) {
                    defs.add(new RawStmtDef(defaultName, (String) o));
                } else if (o instanceof Map) {
                    RawStmtDef def = new RawStmtDef(defaultName, (Map<String, Object>) o);
                    defs.add(def);
                } else {
                    throw new RuntimeException("Can not construct stmt def from object type:" + o.getClass());
                }
            }
            this.setRawStmtDefs(defs);
        } else if (object instanceof Map) {
            Map<String,Object> map = (Map<String,Object>) object;
            List<Map<String,Object>> itemizedMaps = new ArrayList<>();
            for (Map.Entry<String, Object> entries : map.entrySet()) {
                Object value = entries.getValue();
                if (value instanceof LinkedHashMap) {
                    // reset order to favor naming first
                    LinkedHashMap<String, Object> vmap = (LinkedHashMap<String, Object>) value;
                    LinkedHashMap<String, Object> cp = new LinkedHashMap<>(vmap);
                    vmap.clear();
                    vmap.put("name", entries.getKey());
                    vmap.putAll(cp);
                    itemizedMaps.add(vmap);
                } else if (value instanceof String) {
                    Map<String, Object> stmtDetails = new HashMap<>() {{
                        put("name", entries.getKey());
                        put("stmt", entries.getValue());
                    }};
                    itemizedMaps.add(stmtDetails);
                } else {
                    throw new RuntimeException("Unknown inner value type on map-based statement definition: name:'" + entries.getKey() + "', type:'" +
                    entries.getValue().getClass() + "', only maps and strings are recognized.");
                }
            }
            setStatementsFieldByType(itemizedMaps);
        } else if (object instanceof String) {
            setStatementsFieldByType(Map.of("stmt1", (String) object));
        } else {
            throw new RuntimeException("Unknown object type: " + object.getClass());
        }
    }

}
