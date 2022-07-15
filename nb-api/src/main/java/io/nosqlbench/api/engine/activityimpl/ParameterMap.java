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

package io.nosqlbench.api.engine.activityimpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.api.engine.util.Unit;
import io.nosqlbench.api.config.params.ParamsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.script.Bindings;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>A concurrently accessible parameter map which holds both keys and values as strings.
 * An atomic change counter tracks updates, to allow interested consumers to determine
 * when to re-read values across threads. The basic format is
 * &lt;paramname&gt;=&lt;paramvalue&gt;;...</p>
 *
 * <p>To create a parameter map, use one of the static parse... methods.</p>
 *
 * <p>No non-String types are used internally. Everything is encoded as a String, even though the
 * generic type is parameterized for Bindings support.</p>
 */
public class ParameterMap extends ConcurrentHashMap<String,Object> implements Bindings, ProxyObject {
    private final static Logger logger = LogManager.getLogger("PARAMS");
    private final static Gson gson = new GsonBuilder().create();


//    private final ConcurrentHashMap<String, String> paramMap = new ConcurrentHashMap<>(10);
    private final AtomicLong changeCounter;
    private final LinkedList<Listener> listeners = new LinkedList<>();

    public ParameterMap(Map<String, String> valueMap) {
        logger.trace("new parameter map:" + valueMap.toString());
        this.changeCounter=new AtomicLong(0L);
        putAll(valueMap);
    }

    public void assertOnlyOneOf(String... paramName) {
        Object[] objects = Arrays.stream(paramName).map(super::get).filter(Objects::nonNull).toArray();
        if (objects.length>1) {
            throw new RuntimeException("Multiple incompatible parameters are specified: " + Arrays.toString(paramName)
                    + ". Just use one of them.");
        }
    }

    public Optional<String> getOptionalString(String... paramName) {
        Object[] objects = Arrays.stream(paramName).map(super::get).filter(Objects::nonNull).toArray();
        if (objects.length>1) {
         throw new RuntimeException("Multiple parameters are specified for the same value: " + Arrays.toString(paramName)
                 + ". Just use one of them.");
        }
        return Arrays.stream(objects).map(String::valueOf).findAny();
        //return Optional.ofNullable(super.get(paramName)).map(String::valueOf);
    }

    public Optional<String> removeOptionalString(String... paramName) {
        Optional<String> optionalString = getOptionalString(paramName);
        if (optionalString.isPresent()) {
            for (String s : paramName) {
                remove(s);
            }
        }
        return optionalString;
    }


    public Optional<NamedParameter> getOptionalNamedParameter(String... paramName) {
        List<String> defined = Arrays.stream(paramName).filter(super::containsKey).collect(Collectors.toList());
        if (defined.size()==1) {
            return Optional.of(new NamedParameter(defined.get(0),String.valueOf(super.get(defined.get(0)))));
        }
        if (defined.size()>1) {
            throw new RuntimeException("Multiple incompatible parameter names are specified: " + Arrays.toString(paramName)
                    + ". Just use one of them.");
        }
        return Optional.empty();
    }


    public Optional<Long> getOptionalLong(String paramName) {
        return Optional.ofNullable(super.get(paramName)).map(String::valueOf).map(Long::valueOf);
    }

    public Optional<Long> getOptionalMillisUnit(String paramName) {
        return getOptionalString(paramName).flatMap(Unit::msFor);
    }

    public Optional<Long> getOptionalLongUnitCount(String paramName) {
        return getOptionalString(paramName).flatMap(Unit::longCountFor);
    }

    public Optional<Double> getOptionalDoubleUnitCount(String paramName) {
        return getOptionalString(paramName).flatMap(Unit::doubleCountFor);
    }

    public Optional<Long> getOptionalLongBytes(String paramName) {
        return getOptionalDoubleBytes(paramName).map(Double::longValue);
    }

    public Optional<Double> getOptionalDoubleBytes(String paramName) {
        return getOptionalString(paramName).flatMap(Unit::bytesFor);
    }

    public Optional<Double> getOptionalDouble(String paramName) {
        return Optional.ofNullable(super.get(paramName)).map(String::valueOf).map(Double::valueOf);
    }

    public Optional<Boolean> getOptionalBoolean(String paramName) {
        return Optional.ofNullable(super.get(paramName)).map(String::valueOf).map(Boolean::valueOf);
    }

    public Long takeLongOrDefault(String paramName, Long defaultLongValue) {
        Optional<String> l = Optional.ofNullable(super.remove(paramName)).map(String::valueOf);
        Long lval = l.map(Long::valueOf).orElse(defaultLongValue);
        markMutation();
        return lval;
    }

    public Double takeDoubleOrDefault(String paramName, double defaultDoubleValue) {
        Optional<String> d = Optional.ofNullable(super.remove(paramName)).map(String::valueOf);
        Double dval = d.map(Double::valueOf).orElse(defaultDoubleValue);
        markMutation();
        return dval;
    }

    public String takeStringOrDefault(String paramName, String defaultStringValue) {
        Optional<String> s = Optional.ofNullable(super.remove(paramName)).map(String::valueOf);
        String sval = s.orElse(defaultStringValue);
        markMutation();
        return sval;
    }

    public int takeIntOrDefault(String paramName, int paramDefault) {
        Optional<String> i = Optional.ofNullable(super.remove(paramName)).map(String::valueOf);
        int ival = i.map(Integer::valueOf).orElse(paramDefault);
        markMutation();
        return ival;
    }

    public boolean takeBoolOrDefault(String paramName, boolean defaultBoolValue) {
        Optional<String> b = Optional.ofNullable(super.remove(paramName)).map(String::valueOf);
        boolean bval = b.map(Boolean::valueOf).orElse(defaultBoolValue);
        markMutation();
        return bval;
    }


    @Override
    public Object get(Object key) {
        logger.trace("getting parameter " + key);
        return super.get(key);
    }

    public void setSilently(String paramName, Object newValue) {
        super.put(paramName, String.valueOf(newValue));
        logger.trace("setting param silently " + paramName + "=" + newValue);
    }


    public void set(String paramName, Object newValue) {
        super.put(paramName, String.valueOf(newValue));
        logger.info("setting param " + paramName + "=" + newValue);
        markMutation();
    }

    private static final Pattern encodedParamsSquote = Pattern.compile("(?<param>\\w+?)='(?<value>[^']+?);");
    private static final Pattern encodedParamsDquote = Pattern.compile("(?<param>\\w+?)=\"(?<value>[^\"]+?);");
    private static final Pattern encodedParamsPattern = Pattern.compile("(?<param>\\w+?)=(?<value>.+?);");

    @Override
    public Object put(String name, Object value) {
        Object oldVal = super.put(name, String.valueOf(value));
        logger.info("putting param " + name + "=" + value);

        markMutation();
        return oldVal;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        for (Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {

            Object raw = entry.getValue();
            String value = null;
            if (raw instanceof Map || raw instanceof Set || raw instanceof List) {
                value = gson.toJson(raw);
            } else {
                value = raw.toString();
            }
            super.put(entry.getKey(), value);
        }
        markMutation();
    }

    @Override
    public Object remove(Object key) {
        Object removed = super.remove(key);
        logger.trace("removing param: " + key);

        markMutation();
        return removed;
    }

    @Override
    public void clear() {
        logger.debug("parameter map cleared:" + this);
        super.clear();

        markMutation();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        logger.debug("getting entry set for " + this);
        return super.entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<String,Object>(e.getKey(), e.getValue()) {})
                .collect(Collectors.toCollection(HashSet::new));
    }


    private void markMutation() {
        changeCounter.incrementAndGet();
        logger.trace("calling " + listeners.size() + " listeners.");
        callListeners();
    }

    /**
     * Get the atomic change counter for this parameter map.
     * It getes incremented whenever any changes are made to the map.
     *
     * @return the atomic long change counter
     */
    public AtomicLong getChangeCounter() {
        return changeCounter;
    }

    public String toString() {
        return "(" + this.changeCounter.get() + ")/" + super.toString();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void callListeners() {
        for (Listener listener : listeners) {
            logger.trace("calling listener:" + listener);
            listener.handleParameterMapUpdate(this);
        }
    }

    public int getSize() {
        return super.size();
    }

    public static ParameterMap parseOrException(String encodedParams) {
        if (encodedParams == null) {
            throw new RuntimeException("Must provide a non-null String to parse parameters.");
        }
        Map<String, String> parsedMap = ParamsParser.parse(encodedParams,true);
        return new ParameterMap(parsedMap);
    }

    public static Optional<ParameterMap> parseParams(String encodedParams) {
        try {
            return Optional.ofNullable(parseOrException(encodedParams));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getOptionalInteger(String paramName) {
        return getOptionalString(paramName).map(Integer::valueOf);
    }

    @Override
    public Object getMember(String key) {
        return this.get(key);
    }

    @Override
    public Object getMemberKeys() {
        return new ArrayList<>(this.keySet());
    }

    @Override
    public boolean hasMember(String key) {
        return this.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        this.put(key,value);
    }

    @Override
    public boolean removeMember(String key) {
        Object removed = this.remove(key);
        return removed!=null;
    }


    public interface Listener {
        void handleParameterMapUpdate(ParameterMap parameterMap);
    }

    public Map<String,String> getStringStringMap() {
        return new HashMap<String,String>() {{
            for (Entry entry : ParameterMap.this.entrySet()) {
                put(entry.getKey().toString(),entry.getValue().toString());
            }
        }};
    }

    public static class NamedParameter {
        public final String name;
        public final String value;

        public NamedParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public String toString() {
            return name+"="+value;
        }
        public String getName() {
            return name;
        }
        public String getValue() {
            return value;
        }
    }

    public static String toJSON(Map<?,?> map) {
        List<String> l = new ArrayList<>();
        map.forEach((k,v) -> l.add("'" + k + "': '" + v + "'"));
        return "params={"+String.join(",\n  ",l)+"};\n";
    }

}
