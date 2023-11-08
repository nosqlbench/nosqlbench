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

package io.nosqlbench.engine.api.templating;

import io.nosqlbench.engine.api.scenarios.NBForEachCombination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;

public class NBForEachCombinationsTest {

    @Test
    public void testAddAndGetKeys() {
        NBForEachCombination combinations = new NBForEachCombination();
        combinations.add("key1", "value1,value2");
        combinations.add("key2", "value3,value4");

        List<String> keys = combinations.getKeys();

        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }

    @Test
    public void testIteratorWithoutNames() {
        NBForEachCombination combinations = new NBForEachCombination();
        combinations.add("key1", "value1,value2");
        combinations.add("key2", "value3,value4");

        Iterator<NBForEachCombination.NBForEach> iterator = combinations.iterator(false);
        int count = 0;
        while (iterator.hasNext()) {
            NBForEachCombination.NBForEach combination = iterator.next();
            count++;
        }

        assertEquals(4, count); // 2 values for key1 * 2 values for key2 = 4 combinations
    }

    @Test
    public void testIteratorWithNames() {
        NBForEachCombination combinations = new NBForEachCombination();
        combinations.add("key1", "value1,value2");
        combinations.add("key2", "value3,value4");

        Iterator<NBForEachCombination.NBForEach> iterator = combinations.iterator(true);
        int count = 0;
        while (iterator.hasNext()) {
            NBForEachCombination.NBForEach combination = iterator.next();
            String fields = combination.getFields();
            assertNotNull(fields);
            assertTrue(fields.contains("key1") || fields.contains("key2"));
            assertTrue(fields.contains("value1") || fields.contains("value2") ||
                       fields.contains("value3") || fields.contains("value4"));
            count++;
        }

        assertEquals(4, count); // 2 values for key1 * 2 values for key2 = 4 combinations
    }

    @Test
    public void testEmptyCombinations() {
        NBForEachCombination combinations = new NBForEachCombination();

        Iterator<NBForEachCombination.NBForEach> iterator = combinations.iterator(false);
        assertFalse(iterator.hasNext());
    }
}
