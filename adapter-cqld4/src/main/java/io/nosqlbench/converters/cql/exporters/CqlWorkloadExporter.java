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

package io.nosqlbench.converters.cql.exporters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.converters.cql.cqlast.CqlColumnDef;
import io.nosqlbench.converters.cql.cqlast.CqlKeyspace;
import io.nosqlbench.converters.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cqlast.CqlTable;
import io.nosqlbench.converters.cql.parser.CqlModelParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The unit of generation is simply everything that is provided to the exporter together.
 * Thus if you feed it one create statement at a time, it will yield a workload with just that,
 * and if you feed it an elaborate schema, it will generate a workload inclusive of all
 * provided elements.
 */
public class CqlWorkloadExporter {

    private final static Logger logger = LogManager.getLogger(CqlWorkloadExporter.class);

    private final CqlModel model;

    public CqlWorkloadExporter(CqlModel model) {
        this.model = model;
    }

    public CqlWorkloadExporter(String ddl, Path srcpath) {
        this.model = CqlModelParser.parse(ddl, srcpath);
    }

    public CqlWorkloadExporter(String ddl) {
        this.model = CqlModelParser.parse(ddl, null);
    }

    public CqlWorkloadExporter(Path path) {
        this.model = CqlModelParser.parse(path);
    }


    public static void main(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("Usage example: PROG filepath.cql filepath.yaml");
        }
        Path srcpath = Path.of(args[0]);
        if (!srcpath.toString().endsWith(".cql")) {
            throw new RuntimeException("File '" + srcpath + "' must end in .cql");
        }
        if (!Files.exists(srcpath)) {
            throw new RuntimeException("File '" + srcpath + "' does not exist.");
        }

        Path target = Path.of(srcpath.toString().replace("\\.cql", "\\.yaml"));
        if (args.length == 2) {
            target = Path.of(args[1]);
        }
        if (!target.toString().endsWith(".yaml")) {
            throw new RuntimeException("Target file must end in .yaml");
        }
        if (Files.exists(target) && !target.toString().startsWith("_")) {
            throw new RuntimeException("Target file '" + target + "' exists. Please remove it first or use a different target file name.");
        }

        CqlWorkloadExporter exporter = new CqlWorkloadExporter(srcpath);
        String workload = exporter.getWorkloadAsYaml();
        try {
            Files.write(
                target,
                workload.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getWorkload() {
        Map<String, Object> workload = new LinkedHashMap<>();

        for (CqlKeyspace ks : model.getKeyspaces().values()) {
            workload.put("bindings", getDefaultCqlBindings());
            Map<String, Object> blocks = new LinkedHashMap<>();
            workload.put("blocks", blocks);
            blocks.put("schema", genSchemaBlock(model));
            blocks.put("rampup", genRampupBlock(model));
            blocks.put("main", genMainBlock(model));
        }
        return workload;
    }

    private Map<String, Object> genMainBlock(CqlModel model) {
        Map<String, String> mainOpTemplates = new LinkedHashMap<>();

        mainOpTemplates.putAll(
            model.getAllTables()
                .stream()
                .collect(Collectors.toMap(
                    t -> "insert-" + t.getKeySpace()+"__"+t.getTableName(),
                    this::genUpsertTemplate)
                )
        );

        mainOpTemplates.putAll(
            model.getAllTables()
                .stream()
                .collect(Collectors.toMap(
                    t -> "select-" + t.getKeySpace()+"__"+t.getTableName(),
                    this::genSelectTemplate)
                ));


        return Map.of("ops", mainOpTemplates);
    }


    private Map<String, Object> genRampupBlock(CqlModel model) {
        Map<String, String> rampupOpTemplates = model.getAllTables()
            .stream()
            .collect(Collectors.toMap(
                t -> "insert-" + t.getKeySpace()+"__"+t.getTableName(),
                this::genUpsertTemplate)
            );

        return Map.of("ops", rampupOpTemplates);
    }

    private String genSelectTemplate(CqlTable table) {
        List<CqlColumnDef> cdefs = table.getColumnDefinitions();
        return "select * from " + table.getKeySpace() + "." + table.getTableName() +
            "\n WHERE " + genPredicateTemplate(table);

    }

    private String genPredicateTemplate(CqlTable table) {
        return table.getColumnDefinitions()
            .stream()
            .map(this::genPredicatePart)
            .collect(Collectors.joining("\n AND "))
            + ";";

    }

    private String genPredicatePart(CqlColumnDef def) {
        String typeName = def.getType();

        CqlLiteralFormat cqlLiteralFormat =
            CqlLiteralFormat.valueOfCqlType(typeName).orElse(CqlLiteralFormat.UNKNOWN);
        if (cqlLiteralFormat == CqlLiteralFormat.UNKNOWN) {
            logger.warn("Unknown literal format for " + typeName);
        }

        return def.getName() + "=" + cqlLiteralFormat.format("{" + def.getName() + "}");
    }


    private String genUpsertTemplate(CqlTable table) {
        List<CqlColumnDef> cdefs = table.getColumnDefinitions();
        return "insert into " + table.getKeySpace() + "." + table.getTableName() + "\n ( "
            + cdefs.stream().map(cd -> cd.getName()).collect(Collectors.joining(" , ")) +
            " )\n values\n (" + cdefs.stream().map(cd -> cd.getName()).collect(Collectors.joining("},{", "{", "}"))
            + ");";
    }

    public String getWorkloadAsYaml() {
        DumpSettings dumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setMaxSimpleKeyLength(100)
            .setSplitLines(true)
            .setIndentWithIndicator(true)
            .setMultiLineFlow(true)
            .setNonPrintableStyle(NonPrintableStyle.ESCAPE)
            .build();
        BaseRepresenter r;
        Dump dump = new Dump(dumpSettings);
        Map<String, Object> workload = getWorkload();
        return dump.dumpToString(workload);
    }

    public String getModelAsJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(model);
    }

    public String getWorkoadAsJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> workload = getWorkload();
        return gson.toJson(workload);
    }

    private Object genTableCrudTemplates(CqlTable table) {
        return Map.of();
    }

    private Map<String, Object> genSchemaBlock(CqlModel model) {
        Map<String, Object> schemablock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();

        for (CqlKeyspace ks : model.getKeyspaces().values()) {
            ops.put("create-keyspace-" + ks.getKeyspaceName(), ks.getRefddl());
        }
        for (String ksname : model.getTablesByKeyspace().keySet()) {
            for (CqlTable cqltable : model.getTablesByKeyspace().get(ksname).values()) {
                ops.put("create-table-" + ksname + "." + cqltable.getTableName(), cqltable.getRefDdl());
            }
        }

        schemablock.put("ops", ops);
        return schemablock;
    }

    private Map<String, Object> getDefaultCqlBindings() {
        return Map.of(
            "text", "NumberNameToString()",
            "timestamp", "yaddayaddayadda"
        );
    }
}
