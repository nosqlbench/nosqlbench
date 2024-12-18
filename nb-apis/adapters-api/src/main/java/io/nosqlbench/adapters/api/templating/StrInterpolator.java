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

import com.google.gson.Gson;
import io.nosqlbench.nb.api.advisor.NBAdvisorBuilder;
import io.nosqlbench.nb.api.advisor.NBAdvisorPoint;
import io.nosqlbench.nb.api.advisor.conditions.Conditions;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.nbio.ResolverChain;
import io.nosqlbench.nb.api.system.NBEnvironment;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class StrInterpolator implements Function<String, String> {
    private final static Logger logger = LogManager.getLogger(StrInterpolator.class);
    private final NBAdvisorBuilder<String> advisorBuilder = new NBAdvisorBuilder<String>();
    private NBAdvisorPoint<String> advisor;
    private final MultiMap multimap = new MultiMap();
    private final StringSubstitutor substitutor =
        new StringSubstitutor(multimap, "<<", ">>", '\\')
            .setEnableSubstitutionInVariables(true)
            .setEnableUndefinedVariableException(true)
            .setDisableSubstitutionInValues(true);
    private final Pattern COMMENT = Pattern.compile("^\\s*#.*");
    private final Pattern INSERT = Pattern.compile("^(\\s*)INSERT:\\s+(.+)$");

    public StrInterpolator(ActivityDef... activityDefs) {
        Arrays.stream(activityDefs)
            .map(ad -> ad.getParams().getStringStringMap())
            .forEach(multimap::add);
    }

    public StrInterpolator(Map<String, ?> basicMap) {
        multimap.add(basicMap);
    }

    public boolean isComment(String line) {
        return line != null && COMMENT.matcher(line).matches();
    }

    // for testing
    protected StrInterpolator(List<Map<String, String>> maps) {
        maps.forEach(multimap::add);
    }

    @Override
    public String apply(String raw) {
        logger.debug(() -> "Applying string transformer to data:\n" + raw);
        advisor = advisorBuilder.build();
        advisor.add(Conditions.DeprecatedWarning);
        List<String> lines = new LinkedList<>(Arrays.asList(raw.split("\\R")));
        boolean endsWithNewline = raw.endsWith("\n");
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (!isComment(line)) {
                String result = matchTemplates(line);
                if (!result.equals(line)) {
                    lines.set(i, result);
                    line = result;
                }
                Matcher matcher = INSERT.matcher(line);
                if (matcher.matches()) {
                    String leadingSpaces = matcher.group(1);
                    String filePath = matcher.group(2);
                    List<String> includes = insertContentFromFile(leadingSpaces, filePath);
                    System.out.println(leadingSpaces + "INSERT: " + filePath);
                    lines.remove(i);
                    lines.addAll(i, includes);
                    i--;
                }
            }
            i++;
        }
        String results = lines.stream().collect(joining(System.lineSeparator()));
        if (endsWithNewline) {
            results += System.lineSeparator();
        }
        advisor.setName("Workload", "Deprecated template format").logName().evaluate();
        String finalResults = results;
        logger.debug(() -> "Results of applying string transformer:\n" + finalResults);
        return results;
    }

    private LinkedList<String> insertContentFromFile(String leadingSpaces, String filePath) {
        // Determine file type and process the inclusion
        LinkedList<String> result = new LinkedList<>();
        result.add(leadingSpaces + "# INSERT: " + filePath);
        try {
            ResolverChain chain = new ResolverChain(filePath);
            Content<?> insert = NBIO.chain(chain.getChain()).searchPrefixes("activities")
                .pathname(chain.getPath()).first()
                .orElseThrow(() -> new RuntimeException("Unable to load path '" + filePath + "'"));
            BufferedReader reader = new BufferedReader(new StringReader(insert.asString()));

            if (filePath.endsWith(".properties")) {
                // Include properties file
                Properties properties = new Properties();
                properties.load(reader);
                for (String key : properties.stringPropertyNames()) {
                    result.add(leadingSpaces + key + ": " + properties.getProperty(key));
                }
            } else if (filePath.endsWith(".json")) {
                // Include JSON
                Gson gson = new Gson();
                Map<String, Object> jsonMap = gson.fromJson(reader, Map.class);
                Yaml yaml = new Yaml();
                String yamlString = yaml.dumpAsMap(jsonMap);
                LinkedList<String> include = new LinkedList<>(Arrays.asList(yamlString.split("\\R")));
                int j = 0;
                while (j < include.size()) {
                    result.add(leadingSpaces + include.get(j));
                    j++;
                }
            } else {
                // Include as a YAML file (if it is not then  if a bad OpDocList is created it will fail.
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(leadingSpaces + line);
                }
            }
        } catch (Exception e) {
            throw new OpConfigError("While processing file '" + filePath + "' " + e.getMessage());
        }
        return result;
    }

    public String matchTemplates(String original) {
        // process TEMPLATE(...)
        String line = original;
        int length = line.length();
        int i = 0;
        while (i < length) {
            if (line.startsWith("${", i)) {
                int start = i + "${".length();
                int openParensCount = 1; // We found one '{' with "${"
                // Find the corresponding closing ')' for this TEMPLATE instance
                int j = start;
                int k = start;
                while (j < length && openParensCount > 0) {
                    if (line.charAt(j) == '{') {
                        openParensCount++;
                    } else if (line.charAt(j) == '}') {
                        k = j;
                        openParensCount--;
                    }
                    j++;
                }
                // check for case of not enough '}'
                if (openParensCount > 0 ) {
                    if ( k != start ) {
                        j = k + 1;
                    }
                    openParensCount = 0;
                }
                // `j` now points just after the closing '}' of this ${
                if (openParensCount == 0) {
                    String templateContent = line.substring(start, j - 1);
                    // Recursively process
                    String templateValue = matchTemplates(templateContent);
                    String resolvedContent = multimap.lookup(templateValue);
                    line = line.substring(0, i) + resolvedContent + line.substring(j);
                    length = line.length();
                    i--;
                }
            } else if (line.startsWith("TEMPLATE(", i)) {
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
            advisor.validate("<<key:value>> in "+line);
            line = after;
            after = substitutor.replace(line);
        }
        return line;
    }

    public Map<String,String> checkpointAccesses() {
        return multimap.checkpointAccesses();
    }

    public static class MultiMap extends StrLookup<String> {

        private final String warnPrefix = "UNSET";
        private final Map<String,String> accesses = new LinkedHashMap<>();
        private final Map<String,String> extractedDefaults = new LinkedHashMap<>();
        private final Map<String,String> overrides = new LinkedHashMap<>();
        private final List<Map<String, ?>> maps = new ArrayList<>();

        public void add(Map<String, ?> addedMap) {
            maps.add(addedMap);
        }

        @Override
        public String lookup(String key) {
            //String original = key;
            String value = null;
            char substitution = ' ';

            String[] parts = key.split("[:,]", 2);
            if (parts.length == 2) {
                key = parts[0];
                value = parts[1];
                if ( value.length() > 0 ) {
                    substitution = value.charAt(0);
                } else {
                    substitution = ' ';
                }
                if ( substitution == '-' || substitution == '=' || substitution == '?' || substitution == '+') {
                    if ( value.length() < 2 ) {
                        value = null;
                    } else {
                        value = value.substring(1);
                    }
                } else {
                    substitution = ' ';
                }
                if (!extractedDefaults.containsKey(key)) {
                    extractedDefaults.put(key, value);
                }
                if (!overrides.containsKey(key)) {
                    if ( substitution == '=' ) {
                        overrides.put(key, value);
                        //System.out.println(key+"="+value);
                    } else if ( substitution == '?' ) {
                        throw new NullPointerException("Parameter "+key+" is not set");
                    }
                }
            }

            if ( substitution != '+' ) {
                Object val = overrides.get(key);
                if (val != null) {
                    value = val.toString();
                    //System.out.println("for: '"+original+"': "+key+"->"+value);
                } else {
                    boolean check_env = true;
                    for (Map<String, ?> map : maps) {
                        val = map.get(key);
                        if (val != null) {
                            value = val.toString();
                            check_env = false;
                            break;
                        }
                    }
                    if (check_env && NBEnvironment.INSTANCE.hasPropertyLayer() && NBEnvironment.INSTANCE.containsKey(key) ) {
                        value = NBEnvironment.INSTANCE.get(key);
                    }
                }
                value = (value==null? extractedDefaults.get(key) : value);
            }

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
