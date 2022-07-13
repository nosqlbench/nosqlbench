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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The unit of generation is simply everything that is provided to the exporter together.
 * Thus if you feed it one create statement at a time, it will yield a workload with just that,
 * and if you feed it an elaborate schema, it will generate a workload inclusive of all
 * provided elements.
 */
public class CqlWorkloadExporter {
    private final static Logger logger = LogManager.getLogger(CqlWorkloadExporter.class);
    public final static String DEFAULT_NAMING_TEMPLATE = "[OPTYPE-][COLUMN-][TYPEDEF-][TABLE!]-[KEYSPACE]";

    private final BindingsLibrary defaultBindings = new DefaultCqlBindings();

    private final NamingFolio namer = new NamingFolio(DEFAULT_NAMING_TEMPLATE);
    private final BindingsAccumulator bindings = new BindingsAccumulator(namer, List.of(defaultBindings));
    private final CqlModel model;
    private final Map<String, String> bindingsMap = new LinkedHashMap<>();

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
        logger.info("running CQL workload exporter with args:" + Arrays.toString(args));

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

        Path target = null;
        if (args.length == 2) {
            target = Path.of(args[1]);
            logger.info("using output path as '" + target + "'");
        } else {
            target = Path.of(srcpath.toString().replace(".cql", ".yaml"));
            logger.info("assumed output path as '" + target + "'");

        }

        if (!target.toString().endsWith(".yaml")) {
            throw new RuntimeException("Target file must end in .yaml, but it is '" + target + "'");
        }
        if (Files.exists(target) && !target.toString().startsWith("_")) {
            throw new RuntimeException("Target file '" + target + "' exists. Please remove it first or use a different target file name.");
        }

        CqlWorkloadExporter exporter = new CqlWorkloadExporter(srcpath);
        String workload = exporter.getWorkloadAsYaml();
        try {
            Files.writeString(
                target,
                workload,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getWorkload() {
        namer.populate(model);

        Map<String, Object> workload = new LinkedHashMap<>();
        workload.put("bindings", bindingsMap);
        Map<String, Object> blocks = new LinkedHashMap<>();
        workload.put("blocks", blocks);
        blocks.put("schema", genSchemaBlock(model));
        blocks.put("rampup", genRampupBlock(model));
        blocks.put("main", genMainBlock(model));
        bindingsMap.putAll(bindings.getAccumulatedBindings());
        return workload;
    }

    private Map<String, Object> genMainBlock(CqlModel model) {
        Map<String, String> mainOpTemplates = new LinkedHashMap<>();

        mainOpTemplates.putAll(
            model.getAllTables()
                .stream()
                .collect(Collectors.toMap(
                    t -> namer.nameFor(t, "optype", "insert"),
                    this::genUpsertTemplate))
        );

        mainOpTemplates.putAll(
            model.getAllTables()
                .stream()
                .collect(Collectors.toMap(
                    t -> namer.nameFor(t, "optype", "select"),
                    this::genSelectTemplate)
                ));


        return Map.of("ops", mainOpTemplates);
    }


    private Map<String, Object> genRampupBlock(CqlModel model) {
        Map<String, String> rampupOpTemplates = model.getAllTables()
            .stream()
            .collect(Collectors.toMap(
                    t -> namer.nameFor(t, "optype", "rampup-insert"),
                    this::genUpsertTemplate
                )
            );

        return Map.of("ops", rampupOpTemplates);
    }

    private String genSelectTemplate(CqlTable table) {

        return "select * from " + table.getKeySpace() + "." + table.getTableName() +
            "\n WHERE " + genPredicateTemplate(table);

    }

    private String genPredicateTemplate(CqlTable table) {
        StringBuilder sb = new StringBuilder();
        List<CqlColumnDef> pkeys = new ArrayList<>();
        for (String pkey : table.getPartitionKeys()) {
            CqlColumnDef coldef = table.getColumnDefForName(pkey);
            pkeys.add(coldef);
        }
        for (String ccol : table.getClusteringColumns()) {
            CqlColumnDef coldef = table.getColumnDefForName(ccol);
            pkeys.add(coldef);
        }

        pkeys.stream().map(this::genPredicatePart)
            .forEach(p -> {
                sb.append(p).append("\n  AND ");
            });
        sb.setLength(sb.length() - "\n  AND ".length());
        return sb.toString();
    }

    private String genPredicatePart(CqlColumnDef def) {
        String typeName = def.getType();

        CqlLiteralFormat cqlLiteralFormat =
            CqlLiteralFormat.valueOfCqlType(typeName).orElse(CqlLiteralFormat.UNKNOWN);
        if (cqlLiteralFormat == CqlLiteralFormat.UNKNOWN) {
            logger.warn("Unknown literal format for " + typeName);
        }

        Binding binding = bindings.forColumn(def);

        return def.getName() + "=" + cqlLiteralFormat.format("{" + binding.name() + "}");
    }


    private String genUpsertTemplate(CqlTable table) {
        List<CqlColumnDef> cdefs = table.getColumnDefinitions();
        return "insert into " +
            table.getKeySpace() + "." + table.getTableName() + "\n" +
            " ( " + cdefs.stream().map(CqlColumnDef::getName)
            .collect(Collectors.joining(" , ")) +
            " )\n values\n (" +
            cdefs
                .stream()
                .map(cd -> {
                    Binding binding = bindings.forColumn(cd);
                    return binding.name();
                })
                .collect(Collectors.joining("},{", "{", "}"))
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

}
