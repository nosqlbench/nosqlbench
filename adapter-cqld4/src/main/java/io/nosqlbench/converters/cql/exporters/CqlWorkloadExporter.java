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
import io.nosqlbench.converters.cql.exporters.binders.*;
import io.nosqlbench.converters.cql.exporters.transformers.CqlModelFixup;
import io.nosqlbench.converters.cql.exporters.transformers.RatioCalculator;
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
import java.util.function.Function;
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
    public static final String DEFAULT_REPLICATION = """
        'class': 'SimpleStrategy',
        'replication_factor': 'TEMPLATE(rf:1)'
        """;

    private final BindingsLibrary defaultBindings = new DefaultCqlBindings();

    private final NamingFolio namer = new NamingFolio(DEFAULT_NAMING_TEMPLATE);
    private final BindingsAccumulator bindings = new BindingsAccumulator(namer, List.of(defaultBindings));
    private CqlModel model;
    private final Map<String, String> bindingsMap = new LinkedHashMap<>();
    private final int DEFAULT_RESOLUTION = 10000;

    public CqlWorkloadExporter(CqlModel model, List<Function<CqlModel, CqlModel>> transformers) {
        this.model = model;
        for (Function<CqlModel, CqlModel> transformer : transformers) {
            this.model = transformer.apply(this.model);
        }
    }

    public CqlWorkloadExporter(String ddl, Path srcpath, List<Function<CqlModel, CqlModel>> transformers) {
        this.model = CqlModelParser.parse(ddl, srcpath);
        for (Function<CqlModel, CqlModel> transformer : transformers) {
            this.model = transformer.apply(this.model);
        }
    }

    public CqlWorkloadExporter(String ddl, List<Function<CqlModel, CqlModel>> transformers) {
        this.model = CqlModelParser.parse(ddl, null);
        for (Function<CqlModel, CqlModel> transformer : transformers) {
            this.model = transformer.apply(this.model);
        }
    }

    public CqlWorkloadExporter(Path path, List<Function<CqlModel, CqlModel>> transformers) {
        this.model = CqlModelParser.parse(path);

        for (Function<CqlModel, CqlModel> transformer : transformers) {
            this.model = transformer.apply(this.model);
        }
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
        if (args.length >= 2) {
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

        CqlSchemaStats schemaStats = null;
        if (args.length == 3) {
            Path statspath = Path.of(args[2]);
            try {
                CqlSchemaStatsParser parser = new CqlSchemaStatsParser();
                schemaStats = parser.parse(statspath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        List<Function<CqlModel, CqlModel>> transformers = List.of(
            new CqlModelFixup(), // elide UDTs in lieu of blobs for now
            new StatsEnhancer(schemaStats), // add keyspace, schema and table stats from nodetool
            new RatioCalculator() // Normalize read and write fraction over total ops in unit interval
        );

        CqlWorkloadExporter exporter = new CqlWorkloadExporter(srcpath, transformers);

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
        workload.put("description", "Auto-generated workload from source schema.");
        workload.put("scenarios", genScenarios(model));
        workload.put("bindings", bindingsMap);
        Map<String, Object> blocks = new LinkedHashMap<>();
        workload.put("blocks", blocks);
        blocks.put("schema", genSchemaBlock(model));
        blocks.put("truncate", genTruncateBlock(model));
        blocks.put("rampup", genRampupBlock(model));
        blocks.put("main", genMainBlock(model));
        bindingsMap.putAll(bindings.getAccumulatedBindings());
        return workload;
    }

    private Map<String, Object> genScenarios(CqlModel model) {
        return Map.of(
            "default", Map.of(
                "schema", "run driver=cql tags=block:schema threads===UNDEF cycles===UNDEF",
                "rampup", "run driver=cql tags=block:rampup threads=auto cycles===TEMPLATE(rampup-cycles,10000)",
                "main", "run driver=cql tags=block:main threads=auto cycles===TEMPLATE(main-cycles,10000)"
            ),
            "truncate", "run driver=cql tags=block:truncate threads===UNDEF cycles===UNDEF"
        );
    }

    private Map<String, Object> genMainBlock(CqlModel model) {
        Map<String, Object> mainOpTemplates = new LinkedHashMap<>();

        for (CqlTable table : model.getAllTables()) {

            if (!isCounterTable(table)) {

                Optional<String> insertTemplate = this.genInsertTemplate(table);
                if (insertTemplate.isPresent()) {
                    mainOpTemplates.put(namer.nameFor(table, "optype", "insert"),
                        Map.of(
                            "stmt", insertTemplate.get(),
                            "ratio", writeRatioFor(table)
                        )
                    );
                } else {
                    throw new RuntimeException("Unable to generate main insert template for table '" + table + "'");
                }
            } else {
                logger.info("skipped insert for counter table '" + table.getTableName() + "'");
            }

            Optional<String> updateTemplate = this.genUpdateTemplate(table);
            if (updateTemplate.isPresent()) {
                mainOpTemplates.put(namer.nameFor(table, "optype", "update"),
                    Map.of(
                        "stmt", updateTemplate.get(),
                        "ratio", writeRatioFor(table)
                    )
                );
            } else {
                throw new RuntimeException("Unable to generate main insert template for table '" + table + "'");
            }


            Optional<String> selectTemplate = this.genSelectTemplate(table);
            if (selectTemplate.isPresent()) {
                mainOpTemplates.put(namer.nameFor(table, "optype", "select"),
                    Map.of("stmt", selectTemplate.get(),
                        "ratio", readRatioFor(table))
                );
            } else {
                throw new RuntimeException("Unable to generate main select template for table '" + table + "'");
            }

        }

        return Map.of("ops", mainOpTemplates);
    }

    private boolean isCounterTable(CqlTable table) {
        return table.getColumnDefinitions().stream()
            .anyMatch(cd -> cd.getTrimmedTypedef().equalsIgnoreCase("counter"));
    }


    private int readRatioFor(CqlTable table) {
        if (table.getTableAttributes().size()==0) {
            return 1;
        }
        double weighted_reads = Double.parseDouble(table.getTableAttributes().get("weighted_reads"));
        return (int) (weighted_reads * DEFAULT_RESOLUTION);
    }

    private int writeRatioFor(CqlTable table) {
        if (table.getTableAttributes().size()==0) {
            return 1;
        }
        double weighted_writes = Double.parseDouble(table.getTableAttributes().get("weighted_writes"));
        return (int) (weighted_writes * DEFAULT_RESOLUTION);
    }


    private Map<String, Object> genRampupBlock(CqlModel model) {
        Map<String, String> rampupOpTemplates = new LinkedHashMap<>();


        for (CqlTable table : model.getAllTables()) {
            if (!isCounterTable(table)) {
                Optional<String> insert = genInsertTemplate(table);
                if (insert.isPresent()) {
                    rampupOpTemplates.put(namer.nameFor(table, "optype", "insert"), insert.get());
                } else {
                    throw new RuntimeException("Unable to create rampup template for table '" + table + "'");
                }
            }
        }

        return Map.of("ops", rampupOpTemplates);
    }

    private Optional<String> genSelectTemplate(CqlTable table) {

        try {
            return Optional.of("select * from " + table.getKeySpace() + "." + table.getTableName() +
                "\n WHERE " + genPredicateTemplate(table));
        } catch (UnresolvedBindingException ube) {
            logger.error(ube);
            return Optional.empty();
        }
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
        String typeName = def.getTrimmedTypedef();
        Binding binding = bindings.forColumn(def);

        return def.getName() + "={" + binding.name() + "}";
    }

    private Optional<String> genUpdateTemplate(CqlTable table) {
        try {

            return Optional.of("""
                 update KEYSPACE.TABLE
                  set ASSIGNMENTS
                  where PREDICATES;
                """
                .replaceAll("KEYSPACE", table.getKeySpace())
                .replaceAll("TABLE", table.getTableName())
                .replaceAll("PREDICATES", genPredicateTemplate(table))
                .replaceAll("ASSIGNMENTS", genAssignments(table)));

        } catch (UnresolvedBindingException ube) {
            return Optional.empty();
        }
    }

    private String genAssignments(CqlTable table) {
        StringBuilder sb = new StringBuilder();
        for (CqlColumnDef coldef : table.getNonKeyColumnDefinitions()) {
            if (coldef.isCounter()) {
                sb.append(coldef.getName()).append("=")
                    .append(coldef.getName()).append("+").append("{").append(bindings.forColumn(coldef).name()).append("}")
                    .append(", ");
            } else {
                sb.append(coldef.getName()).append("=")
                    .append("{").append(bindings.forColumn(coldef).name()).append("}")
                    .append(", ");
            }
        }
        sb.setLength(sb.length() - ", ".length());
        return sb.toString();
    }

    private Optional<String> genInsertTemplate(CqlTable table) {
        try {
            List<CqlColumnDef> cdefs = table.getColumnDefinitions();
            return Optional.of("insert into " +
                table.getKeySpace() + "." + table.getTableName() + "\n" +
                " ( " + cdefs.stream().map(CqlColumnDef::getName)
                .collect(Collectors.joining(" , ")) +
                " )\n values\n (" +
                cdefs
                    .stream()
                    .map(cd -> {
                        Binding binding = bindings.forColumn(cd);
                        return "{" + binding.name() + "}";
                    })
                    .collect(Collectors.joining(","))
                + ");");
        } catch (UnresolvedBindingException ube) {
            return Optional.empty();
        }
    }

    public String getWorkloadAsYaml() {
        DumpSettings dumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setMaxSimpleKeyLength(1000)
            .setWidth(100)
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

    private Map<String, Object> genTruncateBlock(CqlModel model) {
        Map<String, Object> truncateblock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        truncateblock.put("ops", ops);

        for (CqlTable table : model.getAllTables()) {
            ops.put(
                namer.nameFor(table, "optype", "truncate"),
                "truncate " + table.getKeySpace() + "." + table.getTableName() + ";"
            );
        }
        return truncateblock;
    }

    private Map<String, Object> genSchemaBlock(CqlModel model) {
        Map<String, Object> schemablock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();

        for (CqlKeyspace ks : model.getKeyspaces().values()) {
            ops.put("create-keyspace-" + ks.getKeyspaceName(), ks.getRefDdlWithReplFields(DEFAULT_REPLICATION));
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
