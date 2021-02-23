/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.*;

public class StatementsOwner extends RawStmtFields {

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
        Object statementsObject = propsmap.remove("statements");

        if (statementsObject == null) {
            statementsObject = propsmap.remove("statement");
        }

        if (statementsObject != null) {
            setStatementsFieldByType(statementsObject);
        }

//        if (statementsObject!=null) {
//            if (statementsObject instanceof List) {
//                setByObject(statementsObject);
//            } else {
//                throw new RuntimeException("Invalid type for statements property: " + statementsObject.getClass().getCanonicalName());
//            }
//        }
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
                    defs.add(new RawStmtDef(defaultName, (Map<String, Object>) o));
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
                    throw new RuntimeException("Unknown inner value type on map-based statement definition.");
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
