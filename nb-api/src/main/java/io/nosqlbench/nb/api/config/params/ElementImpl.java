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

package io.nosqlbench.nb.api.config.params;

import java.util.*;

/**
 * The source data for a param reader is intended to be a collection of something, not a single value.
 * As such, if a single value is provided, an attempt will be made to convert it from JSON if it starts with
 * object or array notation. If not, the value is assumed to be in the simple ParamsParser form.
 */
public class ElementImpl implements Element {

    private final ElementData data;

    public ElementImpl(ElementData data) {
        this.data = data;
    }

    public String getElementName() {
        String name = data.getGivenName();
        if (name!=null) {
            return name;
        }
        return get(ElementData.NAME, String.class).orElse(null);
    }

    public <T> Optional<T> get(String name, Class<? extends T> classOfT) {
        T found = lookup(data,name, classOfT);
        return Optional.ofNullable(found);
    }

    @Override
    public <T> Optional<T> get(String name) {
        return Optional.ofNullable(data.lookup(name,null));
    }

    private <T> T lookup(ElementData data, String name, Class<T> type) {
        return data.lookup(name,type);
//        int idx=name.indexOf('.');
//        while (idx>0) { // TODO: What about when idx==0 ?
//            String parentName = name.substring(0,idx);
//            if (data.containsKey(parentName)) {
//                Object o = data.get(parentName);
//                ElementData parentElement = DataSources.element(o);
//                String childName = name.substring(idx+1);
//                T found = parentElement.lookup(name,type);
//                if (found!=null) {
//                    return found;
//                }
//            }
//            idx=name.indexOf('.',idx+1);
//        }
//        return data.get(name,type);
    }


    public <T> T getOr(String name, T defaultValue) {
        Class<T> cls = (Class<T>) defaultValue.getClass();
        return get(name, cls).orElse(defaultValue);
    }

    @Override
    public Map<String, Object> getMap() {
        Set<String> keys = this.data.getKeys();
        Map<String, Object> map = new LinkedHashMap<>();

        for (String key : keys) {
            Object value = this.data.get(key);
            map.put(key, value);
        }

        return map;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
