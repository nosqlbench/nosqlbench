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

public class NBForEachCombinations implements Iterable<NBForEachCombinations.NBForEachCombination> {
    private Map<String, List<String>> keyArrays;

    public NBForEachCombinations() {
        keyArrays = new LinkedHashMap<>();
    }

    public void add(String key, String cases) {
        String[] array = cases.split(",");
        add(key, array);
    }

    public void add(String key, String[] array) {
        List<String> expandedArray = new ArrayList<>();
        for (String element : array) {
            expandedArray.add(element);
        }
        keyArrays.put(key, expandedArray);
    }

    public class NBForEachCombination {
	private List<String> elements;

	public NBForEachCombination(List<String> elements) {
	    this.elements = elements;
	}

	public List<String> getCombination() {
	    return elements;
	}

	public String getFields() {
	    String fields = " ";
	    for (String element : elements) {
		fields += element+" ";
	    }
	    return fields;
	}
    }

    public List<NBForEachCombination> getCombinations(boolean includeNames) {
        List<NBForEachCombination> combinations = new ArrayList<>();
        if ( keyArrays.size() < 1 ) return combinations;
        generateCombinationsHelper(new ArrayList<>(keyArrays.keySet()), 0, new ArrayList<>(), combinations, includeNames);
        return combinations;
    }

    public List<String> getKeys() {
        return new ArrayList<>(keyArrays.keySet());
    }

    // Inner class for the Iterator implementation
    public class NBForEachCombinationIterator implements Iterator<NBForEachCombination> {
        private List<NBForEachCombination> combinations = null;
        private int index = 0;

        public NBForEachCombinationIterator(boolean includeNames) {
            combinations = getCombinations(includeNames);
            index = 0;
        }

        @Override
        public boolean hasNext() {
            if (combinations == null) return false;
            return index < combinations.size();
        }

        @Override
        public NBForEachCombination next() {
            if (hasNext()) {
                return combinations.get(index++);
            }
            throw new IllegalStateException("No more combinations");
        }
    }

    @Override
    public Iterator<NBForEachCombination> iterator() {
        return new NBForEachCombinationIterator(false);
    }

    public Iterator<NBForEachCombination> iterator(boolean includeNames) {
        return new NBForEachCombinationIterator(includeNames);
    }

    private void generateCombinationsHelper(List<String> arrayNames, int currentIndex, List<String> currentCombination, List<NBForEachCombination> combinations, boolean includeNames) {
        if (currentIndex == arrayNames.size()) {
            combinations.add(new NBForEachCombination(new ArrayList<>(currentCombination)));
            return;
        }

        String currentArrayName = arrayNames.get(currentIndex);
        List<String> currentArray = keyArrays.get(currentArrayName);
        for (String element : currentArray) {
            if (includeNames) {
                currentCombination.add(currentArrayName + "=" + element);
            } else {
                currentCombination.add(element);
            }
            generateCombinationsHelper(arrayNames, currentIndex + 1, currentCombination, combinations, includeNames);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    //public static void main(String[] args) {

	// For each combinations
    //    NBForEachCombinations foreach = new NBForEachCombinations();

    // datasets
    //    foreach.add("dataset", "glove25,glove50");

    // k
    //    foreach.add("k", "100,75,50,25,10,5,3,1");

    // index
    //    foreach.add("index", "false,true");

    // Get combinations with names included - this is one way to replace setting fields on commands
    //    Iterator<NBForEachCombination> iteratorWithNames = foreach.iterator(true);
    //    while (iteratorWithNames.hasNext()) {
	//    String fields = (iteratorWithNames.next()).getFields();
	//    System.out.println(fields);
    //    }

    // Get key names
    //    List<String> keyNames = foreach.getKeys();
    //    System.out.println("NBForEachCombinations without names:");
    //    System.out.println("Keys: " + keyNames);
    //    System.out.println("Values:");
    // Get combinations without names
    //    for (NBForEachCombination combination : foreach) {
    //        System.out.println(combination.getCombination());
    //    }

    //}
}
