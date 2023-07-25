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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RawOpFields extends Tags {
    private final static Logger logger = LogManager.getLogger(RawOpFields.class);

    private String name = "";
    private String desc = "";
    private final Map<String, String> bindings = new LinkedHashMap<>();
    private final Map<String, Object> params = new LinkedHashMap<>();

    public RawOpFields() {
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    public void setBindings(Map<String, String> bindings) {
        this.bindings.clear();
        this.bindings.putAll(bindings);
    }

    public Map<String,Object> getParams() {
        return this.params;
    }

//    public Map<String, String> getParamsAsText() {
//        Map<String,String> paramsMap = new HashMap<>();
//        this.params.forEach((ko,vo) -> paramsMap.put(ko,vo.toString()));
//        return paramsMap;
//    }

    public void setParams(Map<String, Object> config) {
        this.params.clear();
        this.params.putAll(config);
    }

    public void applyBlockParams(RawOpFields other) {
        setName(other.getName());
        setBindings(other.getBindings());
        setTags(other.getTags());
        setParams(other.getParams());
    }

    @SuppressWarnings("unchecked")
    public void setFieldsByReflection(Map<String, Object> propsmap) {

        Optional.ofNullable(propsmap.remove("desc"))
            .or(() -> Optional.ofNullable(propsmap.remove("description")))
            .map(Object::toString)
            .ifPresent(this::setDescription);

        Object nameObj = propsmap.remove("name");
        if (nameObj!=null) {
            setName(nameObj.toString());
        }

        Object bindingsObject = propsmap.remove("bindings");
        if (bindingsObject!=null) {
            if (bindingsObject instanceof Map) {
                Map<Object, Object> bindingsMap = (Map<Object, Object>) bindingsObject;
                if (bindingsMap != null) {
                    for (Object bkey : bindingsMap.keySet()) {
                        Object bval = bindingsMap.get(bkey);
                        if (bval != null) {
                            bindings.put(bkey.toString(), bval.toString());
                        } else {
                            logger.warn("empty bindings entry for " + bkey);
                        }
                    }
                } else {
                    throw new BasicError("Empty bindings map");
                }
            } else {
              throw new RuntimeException("Invalid type for bindings object: " + bindingsObject.getClass().getCanonicalName());
            }
        }

        Object paramsObject = propsmap.remove("params");
        if (paramsObject!=null) {
            if (paramsObject instanceof Map) {
                Map<Object,Object> paramsMap = (Map<Object,Object>) paramsObject;
                paramsMap.forEach(
                        (ko,vo) -> {
                            String paramkey = ko.toString();
                            Object paramVal = vo;
                            params.put(paramkey, paramVal);
                        }
                );
            } else {
                throw new RuntimeException("Invalid type for params object:" + paramsObject.getClass().getCanonicalName());
            }
        }

        super.setFieldsByReflection(propsmap);
    }
}
