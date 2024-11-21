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

package io.nosqlbench.adapters.api.templating;

import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.advisor.NBAdvisorOutput;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;

public class StrInterpolator implements Function<String, String> {
    private final static Logger logger = LogManager.getLogger(StrInterpolator.class);

    private final MultiMap multimap = new MultiMap();
    private final StringSubstitutor substitutor =
        new StringSubstitutor(multimap, "<<", ">>", '\\')
            .setEnableSubstitutionInVariables(true)
            .setEnableUndefinedVariableException(true)
            .setDisableSubstitutionInValues(true);

    public StrInterpolator(ActivityDef... activityDefs) {
        Arrays.stream(activityDefs)
            .map(ad -> ad.getParams().getStringStringMap())
            .forEach(multimap::add);
    }

    public StrInterpolator(Map<String, ?> basicMap) {
        multimap.add(basicMap);
    }

    // for testing
    protected StrInterpolator(List<Map<String, String>> maps) {
        maps.forEach(multimap::add);
    }

    @Override
    public String apply(String raw) {
        String[] lines = raw.split("\\R");
        boolean endsWithNewline = raw.endsWith("\n");
        int i = 0;
        for (String line : lines) {
            if (!line.startsWith("#")) {
                String result = matchTemplates(line);
                if (!result.equals(line)) {
                    lines[i] = result;
                }
            }
            i++;
        }
        String results = String.join(System.lineSeparator(), lines);
        if (endsWithNewline) {
            results += System.lineSeparator();
        }
        return results;
    }

    public Map<String,String> checkpointAccesses() {
        return multimap.checkpointAccesses();
    }

    public LinkedHashMap<String, String> getTemplateDetails(String input) {
        LinkedHashMap<String, String> details = new LinkedHashMap<>();

        return details;
    }

    public String matchTemplates(String original) {
        // process TEMPLATE(...)
        String line = original;
        int length = line.length();
        int i = 0;
        while (i < length) {
            if (line.startsWith("TEMPLATE(", i)) {
                int start = i + "TEMPLATE(".length();
                int openParensCount = 1; // We found one '(' with "TEMPLATE("
                // Find the corresponding closing ')' for this TEMPLATE instance
                int j = start;
                int k = start;
                while (j < length && openParensCount > 0) {
                    if (line.charAt(j) == '(') {
                        openParensCount++;
                    } else if (line.charAt(j) == ')') {
                        k = j;
                        openParensCount--;
                    }
                    j++;
                }
                // check for case of not enough ')'
                if (openParensCount > 0 ) {
                    if ( k != start ) {
                        j = k + 1;
                    }
                    openParensCount = 0;
                }
                // `j` now points just after the closing ')' of this TEMPLATE
                if (openParensCount == 0) {
                    String templateContent = line.substring(start, j - 1);
                    // Recursively process
                    String templateValue = matchTemplates(templateContent);
                    String resolvedContent = multimap.lookup(templateValue);
                    line = line.substring(0, i) + resolvedContent + line.substring(j);
                    length = line.length();
                    i--;
                }
            }
            i++;
        }
        // Process << ... >>
        String after = substitutor.replace(line);
        while (!after.equals(line)) {
            NBAdvisorOutput.test("<<key:value>> deprecated in "+line);
            line = after;
            after = substitutor.replace(line);
        }
        return line;
    }

    public static class MultiMap extends StrLookup<String> {

        private final List<Map<String, ?>> maps = new ArrayList<>();
        private final String warnPrefix = "UNSET";
        private final Map<String,String> accesses = new LinkedHashMap<>();
        private final Map<String,String> extractedDefaults = new LinkedHashMap<>();

        public void add(Map<String, ?> addedMap) {
            maps.add(addedMap);
        }

        @Override
        public String lookup(String key) {
            String value = null;

            String[] parts = key.split("[:,]", 2);
            if (parts.length == 2) {
                key = parts[0];
                value = parts[1];
                if (!extractedDefaults.containsKey(key)) {
                    extractedDefaults.put(key,value);
                }
            }

            for (Map<String, ?> map : maps) {
                Object val = map.get(key);
                if (val != null) {
                    value = val.toString();
                    break;
                }
            }
            value = (value==null? extractedDefaults.get(key) : value);

            value = (value != null) ? value : warnPrefix + ":" + key;

//            if (accesses.containsKey(key) && !accesses.get(key).equals(value)) {
//                throw new OpConfigError("A templated variable '" + key + "' was found with multiple default values: '" + accesses.get(key) + ", and " + value +". This is not allowed." +
//                    " Template variables must resolve to a single value.");
//            }

            accesses.put(key,value);
            logger.trace("Template parameter '" + key + "' applied as '" + value + "'");
            // TODO summarize these to how many times
            return value;

        }

        public Map<String,String> checkpointAccesses() {
            LinkedHashMap<String,String> accesses = new LinkedHashMap<>(this.accesses);
            logger.trace("removed template params after applying:" + accesses);
            this.accesses.clear();
            return accesses;

        }
    }

}
