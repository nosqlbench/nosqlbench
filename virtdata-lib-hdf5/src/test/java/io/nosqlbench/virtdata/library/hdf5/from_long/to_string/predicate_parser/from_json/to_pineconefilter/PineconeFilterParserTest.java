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
 *
 */

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_pineconefilter;

import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.MultiConditionFilterByLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeFilterParserTest {

    private static String test1 = "{\n" +
        "  \"conditions\": {\n" +
        "    \"and\": [\n" +
        "      {\n" +
        "        \"department_name\": {\n" +
        "          \"EQ\": {\n" +
        "            \"value\": \"Divided Shoes\"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"department_type\": {\n" +
        "          \"EQ\": {\n" +
        "            \"value\": \"Footwear\"\n" +
        "          }\n" +
        "        }\n" +
        "      }\n"  +
        "    ]\n" +
        "  }\n" +
        "}";
    @Test
    public void testComparatorParse() {
        PineconeFilterParser parser = new PineconeFilterParser();
        MultiConditionFilterByLevel mcf = new MultiConditionFilterByLevel(3, true);
        parser.setFilter(mcf);
        String parsed = parser.parse(test1);
        assertEquals("Divided Shoes,Footwear", parsed);
    }

    @Test
    public void testFieldParse() {
        PineconeFilterParser parser = new PineconeFilterParser();
        MultiConditionFilterByLevel mcf = new MultiConditionFilterByLevel(1, false);
        parser.setFilter(mcf);
        String parsed = parser.parse(test1);
        assertEquals("department_name,department_type", parsed);
    }

    @Test
    public void testOperatorParse() {
        PineconeFilterParser parser = new PineconeFilterParser();
        MultiConditionFilterByLevel mcf = new MultiConditionFilterByLevel(2, false);
        parser.setFilter(mcf);
        String parsed = parser.parse(test1);
        assertEquals("EQ,EQ", parsed);
    }

}
