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
import io.nosqlbench.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * See specification for what this should do in UniformWorkloadSpecificationTest
 */
public class RawOpDef extends RawOpFields {
    private final static Logger logger = LogManager.getLogger(RawOpDef.class);

    /**
     * Contains all the op fields. If the key <em>params</em> is used, then fields are divided
     * between the op fields map and the params map, with the non-specified one soaking up the dangling
     * op fields. (Those not under 'op' or 'params' and which are not reserverd words)
     */
    private Object op;

    private final static List<String> opFieldSynonyms = List.of("stmt", "statement", "op", "operation");

    public RawOpDef() {
    }

    public RawOpDef(String name, String op) {
        setName(name);
        this.op = op;
    }

    @SuppressWarnings("unchecked")
    public RawOpDef(String defaultName, Map<String, Object> map) {
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
            String keyName = found.iterator().next();
            Object op = map.remove(keyName);
            if (op instanceof CharSequence s) {
                if (!keyName.equals("stmt")) {
                    logger.warn("Used implied stmt field under name '" + keyName + "'. You can just use 'stmt: ... "+ s +"' or the equivalent to avoid this warning.");
                }
                map.put("stmt",s.toString());
//                setOp(new LinkedHashMap<String,Object>(Map.of("stmt",s.toString())));
            } else {
                setOp(op);
            }
        }
        if (found.size() > 1) {
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
        } else if (op instanceof Map m && m.get("stmt") instanceof CharSequence cs) {
            return cs.toString();
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
