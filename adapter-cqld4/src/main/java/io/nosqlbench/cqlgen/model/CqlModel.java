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

package io.nosqlbench.cqlgen.model;

import io.nosqlbench.cqlgen.core.CGKeyspaceStats;
import io.nosqlbench.cqlgen.core.CGSchemaStats;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>
 * This model contains definition level details for schema elements which are parsed from the
 * Antlr4 CQL grammar.
 * Key elements include:
 * <UL>
 * <LI>keyspace definitions, organized by keyspace name</LI>
 * <li>type definitions, organized by keyspace name</li>
 * <li>table definitions with included column definitions, organized by keyspace name</li>
 * </UL>
 * </p>
 *
 * <p>Because keyspace, table, and type elements are handled sometimes in different ways,
 * these are stored in separate data structures, mapped by the logical keyspace name. This means
 * that you will see table definitions for named keyspaces even if those named keyspaces are not represented
 * in the keyspace definitions. This allows for sub-selecting of rendered elements by logical
 * name without requiring a fully-interconnected keyspace->table->column object graph.
 * </p>
 */
public class CqlModel {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/model");

    private final Supplier<List<String>> errors;
    private final List<CqlKeyspaceDef> keyspaceDefs = new ArrayList();

    private CGSchemaStats schemaStats = null;
    private ComputedSchemaStats computedSchemaStats;
    private Map<String,CqlKeyspaceDef> ksNameCache;

    public CGSchemaStats getStats() {
        return schemaStats;
    }

    public boolean hasStats() {
        return schemaStats != null;
    }

    public ComputedSchemaStats getComputedStats() {
        return computedSchemaStats;
    }

    public void setKeyspaceAttributes(CGSchemaStats schemaStats) {
        this.schemaStats = schemaStats;
        for (String statsKeyspacename : schemaStats.getKeyspaces().keySet()) {
            CGKeyspaceStats keyspaceStats = schemaStats.getKeyspace(statsKeyspacename);

            CqlKeyspaceDef ksdef = getKeyspace(statsKeyspacename);
            if (ksdef !=null) {
                logger.debug(() -> "setting         keyspace stats for '" + statsKeyspacename + "'");
                ksdef.setStats(keyspaceStats);
                keyspaceStats.getKeyspaceTables().forEach((tbname, tbstats) -> {
                    CqlTable table = ksdef.getTable(tbname);
                    if (table != null) {
                        table.setStats(tbstats);
                    } else {
                        logger.debug(() -> " skipping table '" + statsKeyspacename + "." + tbname + ", since it was not found in the model.");
                    }
                });
            } else {
                logger.debug(() -> "       skipping keyspace stats for '" + statsKeyspacename + "'");
            }

        }
    }

    private CqlKeyspaceDef getKeyspace(String ksname) {
        return this.keyspaceDefs.stream().filter(ksd -> ksd.getName().equals(ksname)).findAny().orElse(null);
    }


    public CqlModel(Supplier<List<String>> errorSource) {
        this.errors = errorSource;
    }

    public List<String> getErrors() {
        return errors.get();
    }

    public CqlKeyspaceDef refKeyspace(String ksname) {
        CqlKeyspaceDef keyspace = getKeyspace(ksname);
        if (getKeyspace(ksname)==null) {
            keyspace = new CqlKeyspaceDef(ksname);
            keyspaceDefs.add(keyspace);
        }
        return keyspace;
    }


    public List<CqlKeyspaceDef> getKeyspaceDefs() {
        return this.keyspaceDefs;
    }

    public List<CqlType> getTypeDefs() {
        return this.keyspaceDefs.stream().flatMap(ks -> ks.getTypeDefs().stream()).toList();
    }

    public void removeKeyspaceDef(String ksname) {
        this.keyspaceDefs.remove(ksname);
    }

    public String getSummaryLine() {
        return "keyspaces: " + keyspaceDefs.size() + ", tables: " + getTableDefs().size() +
            ", columns: " + getTableDefs().stream().mapToInt(t -> t.getColumnDefs().size()).sum() +
            ", types: " + getTypeDefs().size();
    }

    public List<CqlTable> getTableDefs() {
        return this.keyspaceDefs.stream().flatMap(ks -> ks.getTableDefs().stream()).toList();
    }

    public void renameColumn(CqlColumnBase extant, String newColName) {
        extant.setName(newColName);
    }

    public boolean isEmpty() {
        return this.keyspaceDefs.size() == 0;
    }

    public List<String> getReferenceErrors() {
        List<String> errors = new ArrayList<>();
        for (CqlKeyspaceDef keyspace : this.keyspaceDefs) {
            keyspace.getReferenceErrors(errors);
        }

        return errors;
    }

    public void addKeyspace(CqlKeyspaceDef keyspace) {
        this.keyspaceDefs.add(keyspace);
    }

    public void addType(String ksname, CqlType usertype) {
        CqlKeyspaceDef refks = this.refKeyspace(ksname);
        usertype.setKeyspace(refks);
        refks.addType(usertype);
    }

    public void addTable(String ksname, CqlTable table) {
        CqlKeyspaceDef cqlKeyspaceDef = refKeyspace(ksname);
        table.setKeyspace(cqlKeyspaceDef);
        cqlKeyspaceDef.addTable(table);
    }
}
