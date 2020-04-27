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

package io.nosqlbench.engine.core.script;

import ch.qos.logback.classic.Logger;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptParams extends HashMap<String, String> {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(ScriptParams.class);

    public Map<String, String> withOverrides(Map<String, String> overrides) {
        if (overrides == null) {
            logger.warn("A null map was provided to withOverrides. This could be a bug in your script.");
            overrides=Map.of();
        }
        checkForNulls("params", "calling withOverrides", this);
        checkForNulls("overrides", "calling withOverrides", overrides);
        HashMap<String, String> result = new HashMap<>();
        result.putAll(this);
        result.putAll(overrides);
        return result;
    }

    public Map<String, String> withDefaults(Map<String, String> defaults) {
        if (defaults == null) {
            logger.warn("A null map was provided to withDefaults. This could be a bug in your script.");
            defaults=Map.of();
        }
        HashMap<String, String> result = new HashMap<>();
        checkForNulls("params", "calling withDefaults", this);
        checkForNulls("defaults", "calling withDefaults", defaults);
        result.putAll(defaults);
        result.putAll(this);
        return result;
    }

    private static void checkForNulls(String name, String action, Map<String, String> map) {
        for (String s : map.keySet()) {
            if (s == null) {
                printMapToLog(name,map);
                throw new BasicError("Found a null key in " + name + " while " + action + ". Please ensure that you " +
                    "only provide non-null keys and values in parameter maps.");
            } else if (map.get(s) == null) {
                printMapToLog(name,map);
                throw new BasicError("Found a null value for key '" + s + "' in " + name + " while " + action + ". " +
                    "Please ensure you provide non-null keys and values in parameter maps.");
            }
        }
    }

    private static void printMapToLog(String name, Map<String, String> map) {
        logger.info("contents of map '" + name + "':");
        String mapdetail = map.entrySet()
            .stream()
            .map(e -> valueOf(e.getKey()) + ":" + valueOf(e.getValue()))
            .collect(Collectors.joining(","));
        logger.info(mapdetail);
    }

    private static String valueOf(Object o) {
        if (o == null) {
            return "NULL";
        }
        return String.valueOf(o);
    }

}
