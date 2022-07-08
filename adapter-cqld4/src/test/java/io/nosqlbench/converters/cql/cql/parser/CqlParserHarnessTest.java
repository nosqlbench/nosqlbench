/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.converters.cql.cql.parser;

import io.nosqlbench.converters.cql.cql.exporters.CqlWorkloadExporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CqlParserHarnessTest {

    private final static String ksddl = """
            CREATE KEYSPACE cycling
            WITH REPLICATION = {
            'class' : 'SimpleStrategy',
            'replication_factor' : 1
            };
            """;
    private final static String tbddl = """
            CREATE TABLE cycling.race_winners (
            race_name text,
            race_position int,
            cyclist_name FROZEN<fullname>,
            PRIMARY KEY (race_name, race_position));
            """;
    private final static String ddl = ksddl + tbddl;

    @Test
    public void testGenBasicWorkload() {
        CqlWorkloadExporter exporter = new CqlWorkloadExporter(ddl);
        var workloadData = exporter.getWorkloadAsYaml();
        System.out.println("# generated workload:\n"+workloadData);
    }

    @Test
    public void testCqlParserHarnessCombined() {
        CqlModelParser.parse(ddl);
    }

    @Disabled
    @Test
    public void testCqlParserHarnessKeyspace() {
        CqlModelParser harness = new CqlModelParser();
        harness.parse("""
            CREATE KEYSPACE cycling
              WITH REPLICATION = {\s
               'class' : 'SimpleStrategy',\s
               'replication_factor' : 1\s
              };
            """);
    }

    @Test
    @Disabled
    public void testCqlParserHarnessTable() {
        CqlModelParser harness = new CqlModelParser();
        harness.parse("""
            CREATE TABLE cycling.race_winners (
               race_name text,\s
               race_position int,\s
               cyclist_name FROZEN<fullname>,\s
               PRIMARY KEY (race_name, race_position));
            """);
    }


}
