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
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.*;

/**
 * See specification for what this should do in UniformWorkloadSpecificationTest
 */
public class RawStmtDef extends RawStmtFields {

    private Object op;

    private final static List<String> opFieldSynonyms = List.of("stmt", "statement", "op", "operation");

    public RawStmtDef() {
    }

    public RawStmtDef(String name, String op) {
        setName(name);
        this.op = op;
    }

    @SuppressWarnings("unchecked")
    public RawStmtDef(String defaultName, Map<String, Object> map) {
        setFieldsByReflection(map);
        if (this.getName() == null || this.getName().isEmpty()) {
            this.setName(defaultName);
        }
    }

    public void setFieldsByReflection(Map<String, Object> map) {
        checkForUnintendedJsonMap(map, new ArrayList<>());
        super.setFieldsByReflection(map);

        HashSet<String> found = new HashSet<>();
        for (String opName : opFieldSynonyms) {
            if (map.containsKey(opName)) {
                found.add(opName);
            }
        }
        if (found.size() == 1) {
            Object op = map.remove(found.iterator().next());
            setOp(op);
        } else if (found.size() > 1) {
            throw new BasicError("You used " + found + " as an op name, but only one of these is allowed at a time.");
        } else if ((getName() == null || getName().isEmpty()) && op == null && map.size() > 0) {
            Map.Entry<String, Object> first = map.entrySet().iterator().next();
            setName(first.getKey());
            setOp(first.getValue());
            map.remove(first.getKey());
        }
        boolean _params = !getParams().isEmpty();
        boolean _op = op != null;

        if (_op) {
            if (_params) {
                if (map.size() > 0) {
                    throw new OpConfigError("If you have scoped op and params, you may not have dangling fields. Op template named '" + this.getName() + "' is invalid. Move dangling params ("+ map.keySet() +") under another field.");
                }
            } else { // no params. Op was a scoped field and there are dangling fields, so assume they belong to params
                getParams().putAll(map);
                map.clear();
            }
        } else { // no op, so assume all remaining fields belong to the op
            LinkedHashMap<String, Object> newop = new LinkedHashMap<>(map);
            setOp(newop);
            map.clear();
        }
    }

    private void setOp(Object op) {
        this.op = op;
    }

    public String getStmt() {
        if (op instanceof CharSequence) {
            return op.toString();
        } else {
            throw new BasicError("tried to access a non-char statement definition with #getStmt()");
        }
    }

    public Object getOp() {
        return op;
    }

    private void setStmt(String statement) {
        this.op = statement;
    }

    public String getName() {
        Object name = getParams().get("name");
        if (name != null) {
            return name.toString();
        }
        return super.getName();
    }

    private void checkForUnintendedJsonMap(Object m, List<String> path) {
        if (m instanceof Map) {
            ((Map)m).forEach((k,v) -> {
                if (v == null) {
                    throw new OpConfigError("A map key '" + k.toString() + "' with a null value was encountered. This is not" +
                        " allowed, and may be the result of using an unquoted binding, like {" + k + "}. You can simply wrap this in quotes" +
                        " like \"{"+ k +"\"} to avoid interpreting this as a JSON map." +
                        (path.size()>0 ? String.join(".",path):""));
                } else {
                    if (v instanceof Map) {
                        path.add(k.toString());
                        checkForUnintendedJsonMap(v, path);
                    }
                }
            });
        }
    }

}
