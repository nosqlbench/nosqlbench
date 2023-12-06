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

package io.nosqlbench.engine.core.lifecycle.scenario.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBCommandParams extends HashMap<String, String> implements ProxyObject {

    private static final Logger logger = LogManager.getLogger(NBCommandParams.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static NBCommandParams of(Map<String,String> params) {
        return new NBCommandParams() {{
            putAll(params);
        }};
    }
    public NBCommandParams withOverrides(Object overrides) {
        Map<String, String> map;
        if (overrides instanceof Map) {
            map = (Map) overrides;
        } else if (overrides instanceof Value v) {
            map = v.as(Map.class);
        } else {
            throw new RuntimeException("Unrecognized overrides type: " + overrides.getClass().getCanonicalName());
        }

        if (overrides == null) {
            logger.warn("A null map was provided to withOverrides. This could be a bug in your script.");
            overrides = Map.of();
        }
        checkForNulls("params", "calling withOverrides", this);
        checkForNulls("overrides", "calling withOverrides", map);
        NBCommandParams result = new NBCommandParams();
        result.putAll(this);

        for (Entry<String, String> overrideEntry : map.entrySet()) {
            String oKey = overrideEntry.getKey();
            String oVal = overrideEntry.getValue();
            if (oVal.toUpperCase().endsWith("UNDEF") || oVal.toUpperCase(Locale.ROOT).endsWith("UNSET")) {
                String removed = result.remove(oKey);
                logger.trace(() -> "Removed key '" + oKey + "': '" + removed + "' from script params because it was " + oVal + " in overrides");
            } else {
                String was = result.get(oKey);
                was = (was == null ? "NULL" : was);
                result.put(oKey, oVal);
                logger.trace("Overrode key '" + oKey + "': from '" + was + " to " + oVal);
            }
        }
        NBCommandParams p = new NBCommandParams();
        p.putAll(result);
        return p;
    }

    public NBCommandParams withDefaults(Object defaults) {
        Map<String, String> map;
        if (defaults instanceof Map) {
            map = (Map) defaults;
        } else if (defaults instanceof Value) {
            map = ((Value) defaults).as(Map.class);
        } else {
            throw new RuntimeException("Unrecognized type for defaults: " + defaults.getClass().getCanonicalName());
        }
        if (map == null) {
            logger.warn("A null map was provided to withDefaults. This could be a bug in your script.");
            map = Map.of();
        }
        NBCommandParams result = new NBCommandParams();
        checkForNulls("params", "calling withDefaults", this);
        checkForNulls("defaults", "calling withDefaults", map);
        result.putAll(map);
        result.putAll(this);
        NBCommandParams p = new NBCommandParams();
        p.putAll(result);
        return p;
    }

    private static void checkForNulls(String name, String action, Map<String, String> map) {
        for (String s : map.keySet()) {
            if (s == null) {
                printMapToLog(name, map);
                throw new BasicError("Found a null key in " + name + " while " + action + ". Please ensure that you " +
                        "only provide non-null keys and values in parameter maps.");
            } else if (map.get(s) == null) {
                printMapToLog(name, map);
                throw new BasicError("Found a null value for key '" + s + "' in " + name + " while " + action + ". " +
                        "Please ensure you provide non-null keys and values in parameter maps.");
            }
        }
    }

    private static void printMapToLog(String name, Map<String, String> map) {
        logger.info(() -> "contents of map '" + name + "':");
        String mapdetail = map.entrySet()
                .stream()
                .map(e -> valueOf(e.getKey()) + ":" + valueOf(e.getValue()))
                .collect(Collectors.joining(","));
        logger.info(mapdetail);
    }

    @Override
    public String toString() {
        return gson.toJson(this, Map.class);
    }

    private static String valueOf(Object o) {
        if (o == null) {
            return "NULL";
        }
        return String.valueOf(o);
    }

    //region ProxyObject methods

    @Override
    public Object getMember(String key) {
        if (key.equals("withDefaults")) {
            return (Function<Object, Object>) this::withDefaults;
        }
        if (key.equals("withOverrides")) {
            return (Function<Object, Object>) this::withOverrides;
        }
        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return
                key.equals("withOverrides")
                        || key.equals("withDefaults")
                        || super.containsKey(key);
    }

    @Override
    public Object getMemberKeys() {
        ArrayList<String> memberKeys = new ArrayList<>(super.keySet());
        memberKeys.add("withDefaults");
        memberKeys.add("withOverride");
        return memberKeys;
    }

    @Override
    public boolean hasMember(String key) {
        if (super.containsKey(key)) {
            return true;
        }
        return key.equals("withOverrides") || key.equals("withDefaults");
    }

    @Override
    public void putMember(String key, Value value) {
        super.put(key, value.asString());
    }

    public Optional<String> maybeGet(String name) {
        return Optional.ofNullable(get(name));
    }
    //endregion

}
