/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_cql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CqlDatasetParserTest {
    String test1 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": 53}}}]}}";
    String test2 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": \"thirteen\"}}}, {\"b\": {\"match\": {\"value\": \"fifty-four\"}}}]}}";
    String test3 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": 13}}}, {\"b\": {\"match\": {\"value\": 54}}},  {\"a\": {\"match\": {\"value\": 154}}}]}}";
    String test4 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": 9}}}, {\"a\": {\"match\": {\"value\": 71}}}]}}";
    String test5 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": 9}}}, {\"a\": {\"match\": {\"value\": 71}}}, {\"a\": {\"match\": {\"value\": 7}}}]}}";
    String test6 = "{\"conditions\": {\"or\": [{\"b\": {\"match\": {\"value\": \"foo\"}}}, {\"b\": {\"match\": {\"value\": \"bar\"}}}]}}";


    @Test
    public void testParse() {
        CqlDatasetParser parser = new CqlDatasetParser();
        String parsed = parser.parse(test1);
        assertEquals("WHERE a=53", parsed);

        parsed = parser.parse(test2);
        assertEquals("WHERE a='thirteen' and b='fifty-four'", parsed);

        parsed = parser.parse(test3);
        assertEquals("WHERE a=13 and b=54 and a=154", parsed);

        parsed = parser.parse(test4);
        assertEquals("WHERE a IN(9,71)", parsed);

        parsed = parser.parse(test5);
        assertEquals("WHERE a IN(9,71,7)", parsed);

        parsed = parser.parse(test6);
        assertEquals("WHERE b IN('foo','bar')", parsed);
    }

}
