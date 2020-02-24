/*
*   Copyright 2017 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.nosqlbench.virtdata.api;

import java.util.*;

public class LazyValuesMap implements Map<String,Object> {

    private final Bindings bindings;
    private final BindingsCache bindingsCache;
    private final long input;

    public LazyValuesMap(Bindings bindings, long input) {
        this.bindings = bindings;
        this.bindingsCache = new BindingsCache(bindings);
        this.input = input;
    }

    @Override
    public int size() {
        return bindings.getTemplate().getBindPointNames().size();
    }

    @Override
    public boolean isEmpty() {
        return bindings.getTemplate().getBindPointNames().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return bindings.getTemplate().getBindPointNames().contains((String) key);
    }

    /**
     * TODO: Doc how this is different, and semantically useful
     * @param value the spec value, not the generated data value
     * @return true if the spec exists in the bindings
     */
    @Override
    public boolean containsValue(Object value) {
        return bindings.getTemplate().getDataMapperSpecs().contains((String) value);
    }

    @Override
    public Object get(Object key) {
        return bindingsCache.getField((String) key,input);
    }

    @Override
    public Object put(String key, Object value) {
        return bindingsCache.getCachedMap().put(key,value);
    }

    @Override
    public Object remove(Object key) {
        return bindingsCache.getCachedMap().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        bindingsCache.getCachedMap().putAll(m);
    }

    @Override
    public void clear() {
        bindingsCache.getCachedMap().clear();
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(new HashSet<String>()
        {{
            addAll(bindings.getTemplate().getBindPointNames());
        }});
    }

    @Override
    public Collection<Object> values() {
        return bindingsCache.getCachedMap().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return bindingsCache.getCachedMap().entrySet();
    }
}
