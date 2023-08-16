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

package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.Metric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.security.InvalidParameterException;
import java.util.*;

public class MetricMap implements ProxyObject {

    private final static Logger logger = LogManager.getLogger(MetricMap.class);
    private final String name;
    private final String parent_name;
    private final HashMap<String, Object> map = new HashMap<>();

    public final static char DELIM = '.';

    public MetricMap(String name, String parent) {
        this.name = name;
        this.parent_name = parent;
    }

    public MetricMap() {
        this("ROOT", "ROOT"); // because of auto-intern, the root node is the only one with parent==parent
    }

    public MetricMap findOrCreateDottedParentPath(String metricName) {
        String[] names = metricName.split("\\.");
        String[] pathTraversal = Arrays.copyOfRange(names, 0, names.length - 1);
        MetricMap owner = findOrCreateNodePath(pathTraversal);
        return owner;
    }

    public MetricMap findOrCreateDottedNodePath(String nodeName) {
        String[] names = nodeName.split("\\.");
        MetricMap owner = findOrCreateNodePath(names);
        return owner;
    }
    @Override
    public String toString() {
        return "MetricMap{" +
            "name='" + name + '\'' +
            ", map=" + map +
            (parent_name != null ? ", parent=" + parent_name : "") +
            '}';
    }

    /**
     * Given an array of non-delimited component names, walk from the root node to each name, creating any needed nodes
     * along the way.
     *
     * @param names the names of the nodes to traverse or create
     * @return The MetricMap node in the node tree with the given path-wise address.
     * @throws InvalidParameterException if any of the component names includes a delimiter
     */
    public MetricMap findOrCreateNodePath(String... names) {
        if (names.length == 0) {
            return this;
        }
        String nodeName = names[0];
        if (nodeName.contains(String.valueOf(DELIM))) {
            throw new InvalidParameterException("Path components must not include interior delimiters. (" + DELIM + ").");
        }
        MetricMap childNode = (MetricMap) map.computeIfAbsent(nodeName, name -> new MetricMap(names[0], this.name));
        return childNode.findOrCreateNodePath(Arrays.copyOfRange(names, 1, names.length));
    }

    public void add(String name, Metric metric) {
        MetricMap owner = findOrCreateDottedParentPath(name);
        String leafName = name.substring(name.lastIndexOf(".") + 1);
        owner.map.put(leafName, metric);
    }

    public void remove(String name) {
        map.remove(name);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Set<String> getKeys() {
        return map.keySet();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Object getMember(String key) {
        if (key.contains(".")) {
            throw new InvalidParameterException("Members of the metrics registry tree must have names which do not include the '.' delimiter.");
        }
        Object got = get(key);
        return got;
    }

    @Override
    public Object getMemberKeys() {
        ArrayList<String> keys = new ArrayList<>(getKeys());
        return keys;
    }

    @Override
    public boolean hasMember(String key) {
        boolean got = getKeys().contains(key);
        return got;
    }

    @Override
    public void putMember(String key, Value value) {
        throw new RuntimeException("Not allowed here");
    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (parent_name != null ? parent_name.hashCode() : 0);
        result = 31 * result + map.hashCode();
        return result;
    }
}
