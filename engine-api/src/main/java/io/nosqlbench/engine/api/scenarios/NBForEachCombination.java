/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.api.scenarios;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NBForEachCombination implements Iterable<NBForEachCombination.NBForEach> {
    private Map<String, List<String>> keyArrays;

    /**
     * Constructs a new NBForEachCombination instance.
     *
     * Example
     * {@code
     *    NBForEachCombination foreach = new NBForEachCombination();
     *
     *    foreach.add("dataset", "glove25,glove50");
     *    foreach.add("k", "100,75,50,25,10,5,3,1");
     *    foreach.add("index", "false,true");
     *
     *    // Get key names
     *    List<String> keyNames = foreach.getKeys();
     *    // Get combinations without names
     *    for (NBForEach combination : foreach) {
     *       System.out.println(combination.getCombination());
     *    }
     * }
     */
    public NBForEachCombination() {
        keyArrays = new LinkedHashMap<>();
    }

    /**
     * Adds an array of values associated with the given key.
     *
     * @param key   The key associated with the array.
     * @param cases The comma-separated string of values.
     */
    public void add(String key, String cases) {
        String[] array = cases.split(",");
        add(key, array);
    }

    /**
     * Adds an array of values associated with the given key.
     *
     * @param key   The key associated with the array.
     * @param array The array of values.
     */
    public void add(String key, String[] array) {
        List<String> expandedArray = new ArrayList<>();
        for (String element : array) {
            expandedArray.add(element);
        }
        keyArrays.put(key, expandedArray);
    }

    /**
     * NBForEachCombination inner class of NBForEachCombinarions instance.
     */
    public class NBForEach {
        /**
         * One single combination of values.
         */
        private List<String> values;

        /**
         * Key value map for consuming this combination
         */
        private LinkedHashMap<String, String> map;

        /**
         * Adds an array of values associated with the combination instance.
         *
         * @param keys   The array of keys for each value
         * @param values The array of values for this combination.
         */
        public NBForEach(List<String> keys, List<String> values) {
            this.values = this.values;
            map = new LinkedHashMap<>();
            // Ensure both lists have the same size
            if (keys.size() != values.size()) {
                throw new IllegalArgumentException("Lists must have the same size");
            }
            // Combine the lists into the LinkedHashMap
            for (int i = 0; i < keys.size(); i++) map.put(keys.get(i), values.get(i));
        }

        /**
         * Adds an array of values associated with the given key.
         *
         * @return The array of values for this combination.
         */
        public List<String> getCombination() {
            return values;
        }

        /**
         * Retrieve a map of values associated with the keys.
         *
         * @return The key-value map for this combination.
         */
        public LinkedHashMap<String, String> getMap() {
            return map;
        }

        /**
         * Process an instance from a naming template.
         *
         * @param template The naming pattern to process
         * @param userData A map of extra key-values to replace in the pattern
         * @return The name for this combination.
         */
        public String getTemplateName(String template, LinkedHashMap<String, String> userData) {
            // Regular expression pattern to match "(key)" where key can be any key from the map
            Pattern pattern = Pattern.compile("\\((\\w+)\\)");
            // Matcher to find the pattern in the input string
            Matcher matcher = pattern.matcher(template);
            // Replace patterns with corresponding values from the map
            StringBuffer resultBuffer = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                if (map.containsKey(key)) {
                    String replacement = map.get(key);
                    matcher.appendReplacement(resultBuffer, replacement);
                } else if (userData.containsKey(key)) {
                    String replacement = userData.get(key);
                    matcher.appendReplacement(resultBuffer, replacement);
                } else {
                    // If the key is not found in the map, keep the original pattern
                    matcher.appendReplacement(resultBuffer, key);
                }
            }
            matcher.appendTail(resultBuffer);
            String string = resultBuffer.toString();
            return string;
        }
    }

    /**
     * Returns an array of the keys in these combinations
     *
     * @return The array of keys.
     */
    public List<String> getKeys() {
        return new ArrayList<>(keyArrays.keySet());
    }

    /**
     * Returns an array of combinations associated with the key array.
     *
     * @return  The array of combinations.
     */
    public List<NBForEach> getCombinations() {
        List<NBForEach> combinations = new ArrayList<>();
        if (keyArrays.isEmpty()) return combinations;
        // Use recursive inner class which creates an array of combinations for each element array added.
        generateCombinationsHelper(new ArrayList<>(keyArrays.keySet()), 0, new ArrayList<>(), combinations);
        return combinations;
    }

    /**
     * For each key array create that layer of combination.
     *
     * @param arrayNames         The names of each key.
     * @param currentIndex       How deep the recursion is in this call.
     * @param currentCombination The current list of combination values being processed.
     * @param combinations       The list of combinations being built.
     */
    private void generateCombinationsHelper(List<String> arrayNames,
                                            int currentIndex,
                                            List<String> currentCombination,
                                            List<NBForEach> combinations) {
        // have we consumed every key?
        if (currentIndex == arrayNames.size()) {
            // add the combination
            combinations.add(new NBForEach(getKeys(), new ArrayList<>(currentCombination)));
            return;
        }

            // get the current key
        String keyName = arrayNames.get(currentIndex);
            // get that key's value array
        List<String> valueArray = keyArrays.get(keyName);
            // iterate on the key's value
        for (String value : valueArray) {
            // add the value to the working combination array
            currentCombination.add(value);
            // generate cases for the next keyName in arrayNames
            generateCombinationsHelper(arrayNames, currentIndex + 1, currentCombination, combinations);
            // remove the last value so that we add the next back on the next iteration
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    /**
     * Inner class for iterating on the list of combinations
     */
    public class NBForEachIterator implements Iterator<NBForEach> {
            /**
             * The list of combinations to be iterated.
             */
        private List<NBForEach> combinations = null;
            /**
             * The current position in the combination list.
             */
        private int index = 0;

        /**
         * Create an Iterator instance
         */
        public NBForEachIterator() {
            index = 0;
            combinations = getCombinations();
        }

        /**
         * Are there more combinations to process?
         *
         * @return if there are more combinations
         */
        @Override
        public boolean hasNext() {
            if (combinations == null) return false;
            return index < combinations.size();
        }

        /**
         * Get the next combination.
         *
         * @return the next combination instance
         */
        @Override
        public NBForEach next() {
            if (hasNext()) {
                return combinations.get(index++);
            }
            throw new IllegalStateException("No more combinations");
        }
    }

    /**
     * Creates an Iterator over Combination values
     *
     * @return a Combination iterator
     */
    @Override
    public Iterator<NBForEach> iterator() {
        return new NBForEachIterator();
    }

}
