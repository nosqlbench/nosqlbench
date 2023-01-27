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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.cqlgen.core.CGKeyspaceStats;
import io.nosqlbench.cqlgen.core.CGSchemaStats;
import io.nosqlbench.cqlgen.core.CGTableStats;
import org.apache.commons.math4.legacy.core.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class CqlSchemaStatsParser {
    private static final String KEYSPACE = "Keyspace";
    private static final String TABLE = "Table";

    CGSchemaStats stats = null;
    CGKeyspaceStats currentKeyspace = null;
    CGTableStats currentTable = null;

    public CGSchemaStats parse(Path statspath) throws IOException {
        this.stats = new CGSchemaStats();
        BufferedReader reader = Files.newBufferedReader(statspath);
        String currentLine = reader.readLine(); //ignore 1st line
        while((currentLine = reader.readLine()) != null) {
            currentLine = currentLine.replaceAll("\t","");
            if (!evalForKeyspace(currentLine)) {
                if (!evalForTable(currentLine)) {
                    String[] splitLine = currentLine.split(":");
                    if (splitLine.length > 1) {
                        Pair<String, String> keyval = new Pair(splitLine[0].trim(), splitLine[1].trim());
                        addAttribute(keyval);
                    }
                }
            }
        }
        writeCurrentTable();
        writeCurrentKeyspace();
        return stats;
    }

    private void addAttribute(Pair<String, String> keyval) {
        if (currentTable != null) {
            currentTable.setAttribute(keyval.getFirst(), keyval.getSecond());
        } else if (currentKeyspace != null) {
            currentKeyspace.setKeyspaceAttribute(keyval.getFirst(), keyval.getSecond());
        } else {
            throw new RuntimeException("Orphaned attribute: " + keyval.toString());
        }
    }

    private boolean evalForTable(String currentLine) {
        if (currentLine.startsWith(TABLE)) {
            writeCurrentTable();
            currentTable = new CGTableStats();
            currentTable.setTableName(currentLine.split(":")[1].trim());
            return true;
        }
        return false;
    }

    private boolean evalForKeyspace(String currentLine) {
        if (currentLine.startsWith(KEYSPACE)) {
            writeCurrentTable();
            writeCurrentKeyspace();
            currentKeyspace = new CGKeyspaceStats();
            currentKeyspace.setKeyspaceName(currentLine.split(":")[1].trim());
            currentTable = null;
            return true;
        }
        return false;
    }

    private void writeCurrentKeyspace() {
        if (currentKeyspace != null) {
            stats.setKeyspace(currentKeyspace);
        }
    }

    private void writeCurrentTable() {
        if (currentTable != null) {
            if (currentKeyspace == null) {
                throw new RuntimeException("Table " + currentTable.getTableName() + "has no associated keyspace");
            } else {
                currentKeyspace.setKeyspaceTable(currentTable.getTableName(), currentTable);
            }
        }
    }
}
