/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.neo4j;

import org.apache.commons.lang3.StringUtils;

import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.ClientException;

import java.util.NoSuchElementException;


public class Neo4JAdapterUtils {

    /**
     * Mask the digits in the given string with '*'
     *
     * @param unmasked The string to mask
     * @return The masked string
     */
    protected static String maskDigits(String unmasked) {
        assert StringUtils.isNotBlank(unmasked) && StringUtils.isNotEmpty(unmasked);
        int inputLength = unmasked.length();
        StringBuilder masked = new StringBuilder(inputLength);
        for (char ch : unmasked.toCharArray()) {
            if (Character.isDigit(ch)) {
                masked.append("*");
            } else {
                masked.append(ch);
            }
        }
        return masked.toString();
    }

    /**
     * Reference:
     * - https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/Value.html#asObject()
     */
    public static Object[] getFieldForAllRecords(Record[] records, String fieldName) {
        int n = records.length;
        Object[] values = new Object[n];
        int idx;
        for (int i = 0; i < n; i++) {
            try {
                idx = records[i].index(fieldName);
                values[i] = records[i].get(idx).asObject();
            }
            catch (NoSuchElementException e) {
                throw e;
            }
            catch (ClientException e) {
                throw e;
            }
        }
        return values;
    }
}
