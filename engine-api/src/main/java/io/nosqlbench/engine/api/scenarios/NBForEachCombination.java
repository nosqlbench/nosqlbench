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
     *    // Get combinations with keys included - this is one way to replace setting fields on commands
     *    Iterator<NBForEachCombination> iteratorWithNames = foreach.iterator(true);
     *    while (iteratorWithNames.hasNext()) {
     *       String fields = (iteratorWithNames.next()).getFields();
     *       System.out.println(fields);
     *    }
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
         * One single combination of elements.
         */
        private List<String> elements;

        /**
         * Adds an array of elements associated with the combination instance.
         *
         * @param elements The array of values for this combination.
         */
        public NBForEach(List<String> elements) {
            this.elements = elements;
        }

        /**
         * Adds an array of values associated with the given key.
         *
         * @return The array of values for this combination.
         */
        public List<String> getCombination() {
            return elements;
        }

        /**
         * Returns a string of values associated with the given key.
         * The results of this depend on combinations being created with names.
         * If so then "key1=value1 key2=value2 key3=value3" type strings are returned.
         *
         * @return The string of values for this combination.
         */
        public String getFields() {
            String fields = " ";
            for (String element : elements) {
                    fields += element+" ";
            }
            return fields;
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
     * @param   includeNames Whether to include the key names as part of each element of each combination.
     * @return  The array of combinations.
     */
    public List<NBForEach> getCombinations(boolean includeNames) {
        List<NBForEach> combinations = new ArrayList<>();
        if (keyArrays.isEmpty()) return combinations;
        // Use recursive inner class which creates an array of combinations for each element array added.
        generateCombinationsHelper(new ArrayList<>(keyArrays.keySet()), 0, new ArrayList<>(), combinations, includeNames);
        return combinations;
    }

    /**
     * For each key array create that layer of combination.
     *
     * @param arrayNames         The names of each key.
     * @param currentIndex       How deep the recursion is in this call.
     * @param currentCombination The current list of combination values being processed.
     * @param combinations       The list of combinations being built.
     * @param includeNames       If true then build each element of a combination as "<key>=<value>"
     */
    private void generateCombinationsHelper(List<String> arrayNames,
                                            int currentIndex,
                                            List<String> currentCombination,
                                            List<NBForEach> combinations,
                                            boolean includeNames) {
        // have we consumed every key?
        if (currentIndex == arrayNames.size()) {
            // add the combination
            combinations.add(new NBForEach(new ArrayList<>(currentCombination)));
            return;
        }

            // get the current key
        String keyName = arrayNames.get(currentIndex);
            // get that key's value array
        List<String> valueArray = keyArrays.get(keyName);
            // iterate on the key's value
        for (String value : valueArray) {
            // add the value to the working combination array
            if (includeNames) {
                currentCombination.add(keyName + "=" + value);
            } else {
                currentCombination.add(value);
            }
            // generate cases for the next keyName in arrayNames
            generateCombinationsHelper(arrayNames, currentIndex + 1, currentCombination, combinations, includeNames);
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
         * Create an Iterator instance with or without keynames.
         */
        public NBForEachIterator(boolean includeNames) {
            index = 0;
            combinations = getCombinations(includeNames);
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
        return new NBForEachIterator(false);
    }

    /**
     * Creates an Iterator over Combination key=values or values
     *
     * @return a Combination iterator
     */
    public Iterator<NBForEach> iterator(boolean includeNames) {
        return new NBForEachIterator(includeNames);
    }

}
