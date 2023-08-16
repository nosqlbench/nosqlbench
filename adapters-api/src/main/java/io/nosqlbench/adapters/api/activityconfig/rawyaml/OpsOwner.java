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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import io.nosqlbench.api.errors.BasicError;

import java.util.*;

public class OpsOwner extends RawOpFields {

    private final static List<String> opsFieldNames = List.of("op","ops","operation","statement","statements");

    private List<RawOpDef> rawOpDefs = new ArrayList<>();

    public OpsOwner() {
    }

    public List<RawOpDef> getRawOpDefs() {
        return rawOpDefs;
    }

    public void setRawStmtDefs(List<RawOpDef> rawOpDefs) {
        this.rawOpDefs = rawOpDefs;
    }

    public void setFieldsByReflection(Map<String, Object> propsmap) {

        // New change
        super.setFieldsByReflection(propsmap);

        HashSet<String> found = new HashSet<>();
        for (String fname : opsFieldNames) {
            if (propsmap.containsKey(fname)) {
                found.add(fname);
            }
        }
        if (found.size()>1) {
            throw new BasicError("You used " + found + " as an op name, but only one of these is allowed.");
        }
        if (found.size()==1) {
            Object opsFieldValue = propsmap.remove(found.iterator().next());
            setOpsFieldByType(opsFieldValue);
        }

        super.setFieldsByReflection(propsmap);
    }

    @SuppressWarnings("unchecked")
    public void setOpsFieldByType(Object object) {
        if (object instanceof List) {
            List<Object> stmtList = (List<Object>) object;
            List<RawOpDef> defs = new ArrayList<>(stmtList.size());
            for (int i = 0; i < stmtList.size(); i++) {
                String defaultName = "stmt" + (i + 1);
                Object o = stmtList.get(i);
                if (o instanceof String) {
                    defs.add(new RawOpDef(defaultName, (String) o));
                } else if (o instanceof Map) {
                    RawOpDef def = new RawOpDef(defaultName, (Map<String, Object>) o);
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
                if (value instanceof List listval) {
                    Map<String,Object> stmtDetails = new LinkedHashMap<>() {{
                        put("name",entries.getKey());
                        put("stmt", (List<Object>) listval);
                    }};
                    itemizedMaps.add(stmtDetails);
                } else if (value instanceof LinkedHashMap vmap) {
                    LinkedHashMap<String, Object> cp = new LinkedHashMap<String,Object>(vmap);
                    vmap.clear();
                    vmap.put("name", entries.getKey());
                    vmap.putAll(cp);
                    itemizedMaps.add(vmap);
                } else if (value instanceof String string) {
                    Map<String, Object> stmtDetails = new LinkedHashMap<>() {{
                        put("name", entries.getKey());
                        put("stmt", string);
                    }};
                    itemizedMaps.add(stmtDetails);
                } else {
                    throw new RuntimeException("Unknown inner value type on map-based statement definition: name:'" + entries.getKey() + "', type:'" +
                    entries.getValue().getClass() + "', only maps and strings are recognized.");
                }
            }
            setOpsFieldByType(itemizedMaps);
        } else if (object instanceof String) {
            setOpsFieldByType(Map.of("stmt1", (String) object));
        } else {
            throw new RuntimeException("Unknown object type: " + object.getClass());
        }
    }

}
