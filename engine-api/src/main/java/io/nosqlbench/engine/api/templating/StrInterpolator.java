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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;

import java.util.*;
import java.util.function.Function;

public class StrInterpolator implements Function<String, String> {

    private MultiMap multimap = new MultiMap();
    private StringSubstitutor substitutor =
        new StringSubstitutor(multimap, "<<", ">>", '\\')
            .setEnableSubstitutionInVariables(true)
            .setEnableUndefinedVariableException(true)
            .setDisableSubstitutionInValues(true);

    private StringSubstitutor substitutor2 =
        new StringSubstitutor(multimap, "TEMPLATE(", ")", '\\')
            .setEnableSubstitutionInVariables(true)
            .setEnableUndefinedVariableException(true)
            .setDisableSubstitutionInValues(true);

    public StrInterpolator(ActivityDef... activityDefs) {
        Arrays.stream(activityDefs)
            .map(ad -> ad.getParams().getStringStringMap())
            .forEach(multimap::add);
    }

    public StrInterpolator(Map<String, String> basicMap) {
        multimap.add(basicMap);
    }

    // for testing
    protected StrInterpolator(List<Map<String, String>> maps) {
        maps.forEach(multimap::add);
    }

    @Override
    public String apply(String raw) {
        String after = substitutor.replace(substitutor2.replace(raw));
        while (!after.equals(raw)) {
            raw = after;
            after = substitutor.replace(substitutor2.replace(raw));
        }
        return after;
    }

    public LinkedHashMap<String, String> getTemplateDetails(String input) {
        LinkedHashMap<String, String> details = new LinkedHashMap<>();

        return details;
    }

    public static class MultiMap extends StrLookup<String> {

        private List<Map<String, String>> maps = new ArrayList<>();
        private String warnPrefix = "UNSET";

        public void add(Map<String, String> addedMap) {
            maps.add(addedMap);
        }

        @Override
        public String lookup(String key) {
            String defval = null;

            String[] parts = key.split("[:,]", 2);
            if (parts.length == 2) {
                key = parts[0];
                defval = parts[1];
            }

            for (Map<String, String> map : maps) {
                String val = map.get(key);
                if (val != null) {
                    return val;
                }
            }

            return (defval != null) ? defval : warnPrefix + ":" + key;
        }
    }


}
