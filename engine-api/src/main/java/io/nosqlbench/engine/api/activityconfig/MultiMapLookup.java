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

package io.nosqlbench.engine.api.activityconfig;

import java.util.*;
import java.util.stream.Collectors;

public class MultiMapLookup implements Map<String, String> {

    private final List<Map<String, String>> maps = new ArrayList<>();

    public MultiMapLookup() {
    }

    public MultiMapLookup(Map<String,String> map1, Map<String,String> map2) {
        add(map1);
        add(map2);
    }

    public MultiMapLookup add(Map<String,String> map) {
        maps.add(map);
        return this;
    }

    @Override
    public int size() {
        long count = maps.stream().map(Map::keySet).flatMap(Set::stream).distinct().count();
        return (int) count;
    }

    @Override
    public boolean isEmpty() {
        return maps.stream().allMatch(Map::isEmpty);
    }

    @Override
    public boolean containsKey(Object key) {
        return maps.stream().anyMatch(m -> m.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return maps.stream().anyMatch(m -> m.containsValue(value));
    }

    @Override
    public String get(Object key) {
        return maps.stream()
                .filter(m -> m.containsKey(String.valueOf(key)))
                .findFirst()
                .map(m -> String.valueOf(m.get(key)))
                .orElse(null);
    }

    @Override
    public String put(String key, String value) {
        throw immutable();
    }

    @Override
    public String remove(Object key) {
        throw immutable();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw immutable();
    }

    @Override
    public void clear() {
        throw immutable();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        maps.stream().map(Map::keySet).flatMap(Set::stream)
                .forEach(keys::add);
        return keys;
    }

    @Override
    public Collection<String> values() {
        return entrySet().stream()
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Map<String, String> compositeMap = new HashMap<>();

        for (Map<String, String> map : maps) {
            for (Entry<String, String> entry : map.entrySet()) {
                if (!compositeMap.containsKey(entry.getKey())) {
                    compositeMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return compositeMap.entrySet();
    }


    private RuntimeException immutable() {
        return new RuntimeException("This map is not meant to be mutable.");
    }

    @Override
    public String toString() {
        return entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue()))
                .collect(Collectors.joining(","));
    }
}
