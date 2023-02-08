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

package io.nosqlbench.cqlgen.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.api.apps.BundledApp;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.cqlgen.api.BindingsLibrary;
import io.nosqlbench.cqlgen.binders.Binding;
import io.nosqlbench.cqlgen.binders.BindingsAccumulator;
import io.nosqlbench.cqlgen.binders.NamingFolio;
import io.nosqlbench.cqlgen.model.*;
import io.nosqlbench.cqlgen.parser.CqlModelParser;
import io.nosqlbench.cqlgen.transformers.CGModelTransformers;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;
import org.yaml.snakeyaml.Yaml;

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
 *
 * @see <a href="https://cassandra.apache.org/doc/trunk/cassandra/cql/index.html">Apache Cassandra CQL Docs</a>
 */
@Service(value = BundledApp.class, selector = "cqlgen")
public class CGWorkloadExporter implements BundledApp {
    public static final String APPNAME = "cqlgen";
    private final static Logger logger = LogManager.getLogger(APPNAME);

    private CGColumnRebinder binder;
    private NamingFolio namer;
    private CqlModel model;

    private final int DEFAULT_RESOLUTION = 10000;

    // TODO: move this to a transformer
    private String replication;

    // TODO: Move these to a config object
    private String namingTemplate;
    private double partitionMultiplier;
    private int quantizerDigits;
    private Map<String, List<String>> blockplan = Map.of();

    private final Map<String, Double> timeouts = new HashMap<String, Double>(Map.of(
            "create", 60.0,
            "truncate", 900.0,
            "drop", 900.0,
            "scan", 30.0,
            "select", 10.0,
            "insert", 10.0,
            "delete", 10.0,
            "update", 10.0
    ));

    public static void main(String[] args) {
        new CGWorkloadExporter().applyAsInt(args);
    }

    @Override
    public int applyAsInt(String[] args) {

        logger.info(() -> "running CQL workload exporter with args:" + Arrays.toString(args));

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

        Yaml yaml = new Yaml();
        CGWorkloadExporter exporter;

        Content<?> cqlgencfg = NBIO.local().prefix("cqlgen").name("cqlgen").extension("conf").first().orElseThrow();
        if (cqlgencfg == null) {
            throw new RuntimeException("Unable to load cqlgen.conf");
        }
        Map cfgmap = yaml.loadAs(cqlgencfg.getInputStream(), Map.class);

        CGModelTransformers modelTransformers = new CGModelTransformers();
        CGTextTransformers textTransformers = new CGTextTransformers();

        if (cfgmap.containsKey("model_transformers")) {
            modelTransformers.accept((List<Map<String, ?>>) cfgmap.get("model_transformers"));
        }

//        Object txtr = cfgmap.get("text_transformers");
//        if (txtr != null) {
//            textTransformers.accept((List<Map<String, ?>>) cfgmap.get("text_transformers"));
//        }

        String ddl;
        try {
            ddl = Files.readString(srcpath);
            logger.info("read " + ddl.length() + " character DDL file, parsing");
            if (textTransformers != null) {
                ddl = textTransformers.process(ddl);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String defaultNamingTemplate = cfgmap.get("naming_template").toString();
        setNamingTemplate(defaultNamingTemplate);

        String partition_multipler = cfgmap.get("partition_multiplier").toString();
        setPartitionMultiplier(Double.parseDouble(partition_multipler));
        configureTimeouts(cfgmap.get("timeouts"));
        configureBlocks(cfgmap.get("blockplan"));
        configureQuantizerDigits(cfgmap.get("quantizer_digits"));

        this.model = CqlModelParser.parse(ddl, srcpath);
        List<String> errorlist = model.getReferenceErrors();
        if (errorlist.size() > 0) {
            for (String error : errorlist) {
                logger.error(error);
            }
            throw new RuntimeException("there were " + errorlist.size() + " reference errors in the model.");
        }
        this.model = modelTransformers.apply(this.model);

        String workload = getWorkloadAsYaml();
        try {
            Files.writeString(
                    target,
                    workload,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );
            logger.info("Wrote workload template as '" + target + "'. Bear in mind that this is simply one version " +
                    "of a workload using this schema, and may not be representative of actual production usage patterns.");
        } catch (IOException e) {
            String errmsg = "There was an error writing '" + target + "'.";
            logger.error(errmsg);
            throw new RuntimeException(errmsg);
        }

        return 0;
    }

    private String loadFile(Path path) {
        try {
            String ddl = Files.readString(path);
            logger.info(() -> "read " + ddl.length() + " character DDL file");
            return ddl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void configureQuantizerDigits(Object quantizer_digits) {
        if (quantizer_digits != null) {
            this.quantizerDigits = Integer.parseInt(quantizer_digits.toString());
        }
    }

    public Map<String, Object> generateBlocks() {

        Map<String, Object> workload = new LinkedHashMap<>();
        workload.put("description", "Auto-generated workload from source schema.");
        workload.put("scenarios", genScenarios(model));
        workload.put("bindings", new LinkedHashMap<String, String>());
        Map<String, Object> blocks = new LinkedHashMap<>();
        workload.put("params", new LinkedHashMap<>(
                Map.of("cl", "LOCAL_QUORUM")
        ));
        workload.put("blocks", blocks);

        for (Map.Entry<String, List<String>> blocknameAndComponents : blockplan.entrySet()) {
            String blockname = blocknameAndComponents.getKey();
            List<String> components = blocknameAndComponents.getValue();

            LinkedHashMap<String, Object> block = new LinkedHashMap<>(
                    Map.of("params", new LinkedHashMap<String, Object>())
            );
            for (String component : components) {
                Map<String, Object> additions = switch (component) {
                    case "schema-keyspaces" -> genCreateKeyspacesOpTemplates(model, blockname);
                    case "schema-tables" -> genCreateTablesOpTemplates(model, blockname);
                    case "schema-types" -> genCreateTypesOpTemplates(model, blockname);
                    case "drop-types" -> genDropTypesBlock(model, blockname);
                    case "drop-tables" -> genDropTablesBlock(model, blockname);
                    case "drop-keyspaces" -> genDropKeyspacesOpTemplates(model, blockname);
                    case "truncate-tables" -> genTruncateTablesOpTemplates(model, blockname);
                    case "insert-seq" -> genInsertOpTemplates(model, blockname);
                    case "select-seq" -> genSelectOpTemplates(model, blockname);
                    case "scan-10-seq" -> genScanOpTemplates(model, blockname);
                    case "update-seq" -> genUpdateOpTemplates(model, blockname);
                    default -> throw new RuntimeException("Unable to create block entries for " + component + ".");
                };
                block.putAll(additions);
            }
            simplifyTimeouts(block);
            blocks.put(blockname, block);
        }
        return workload;
    }

    private void simplifyTimeouts(Map<String, Object> block) {
        Map<Double, List<String>> byTimeout = new LinkedHashMap<>();
        Map<String, Object> ops = (Map<String, Object>) block.get("ops");
        ops.forEach((opname, opmap) -> {
            double timeout = (double) (((Map<String, Object>) opmap).get("timeout"));
            byTimeout.computeIfAbsent(timeout, d -> new ArrayList<>()).add(opname);
        });
        List<Double> timeouts = byTimeout.keySet().stream().sorted(Double::compare).toList();
        if (timeouts.size() == 1) {
            ((Map<String, Object>) block.computeIfAbsent("params", p -> new LinkedHashMap<>())).put("timeout", timeouts.get(0));
            Set<String> opnames = ((Map<String, Object>) block.get("ops")).keySet();
            for (String opname : opnames) {

                Map<String, Object> opmap = (Map<String, Object>) ops.get(opname);
                Map<String, Object> newOp = new LinkedHashMap<>(opmap);
                newOp.remove("timeout");
                ops.put(opname, newOp);
            }
        }
    }

    private void configureBlocks(Object generate_blocks_spec) {
        if (generate_blocks_spec == null) {
            throw new RuntimeException("Error with generate blocks, required parameter 'blockplan' is missing");
        }
        if (generate_blocks_spec instanceof Map blocksmap) {
            Map<String, List<String>> planmap = new LinkedHashMap<>();
            for (Map.Entry<String, String> blockplan : ((Map<String, String>) blocksmap).entrySet()) {
                planmap.put(blockplan.getKey(), Arrays.stream(blockplan.getValue().split(", ")).toList());
            }
            this.blockplan = planmap;
        } else {
            throw new RuntimeException("Unrecognized type '" + generate_blocks_spec.getClass().getSimpleName() + "' for 'blockplan' config.");
        }
    }

    public void configureTimeouts(Object spec) {
        if (spec instanceof Map specmap) {
            for (Object key : specmap.keySet()) {
                if (this.timeouts.containsKey(key.toString())) {
                    Object value = specmap.get(key.toString());
                    if (value instanceof Number number) {
                        this.timeouts.put(key.toString(), number.doubleValue());
                        logger.info("configured '" + key + "' timeout as " + this.timeouts.get(key.toString()) + "S");
                    }

                } else {
                    throw new RuntimeException("timeout type '" + key + "' unknown. Known types: " + this.timeouts.keySet());
                }

            }
        }

    }

    private void setPartitionMultiplier(double multipler) {
        this.partitionMultiplier = multipler;
    }

    public void setNamingTemplate(String namingTemplate) {
        this.namingTemplate = namingTemplate;
    }

    private LinkedHashMap<String, Object> genScenarios(CqlModel model) {
        return new LinkedHashMap<>() {{

            put("default",
                    new LinkedHashMap<>() {{
                        put("schema", "run driver=cql tags=block:\"schema.*\" threads===UNDEF cycles===UNDEF");
                        put("rampup", "run driver=cql tags=block:rampup threads=auto cycles===TEMPLATE(rampup-cycles,10000)");
                        put("main", "run driver=cql tags=block:\"main.*\" threads=auto cycles===TEMPLATE(main-cycles,10000)");
                    }});

            put("main-insert", "run driver=cql tags=block:main-insert threads=auto cycles===TEMPLATE(main-cycles,10000)");
            put("main-select", "run driver=cql tags=block:main-select threads=auto cycles===TEMPLATE(main-cycles,10000)");
            put("main-scan", "run driver=cql tags=block:main-scan threads=auto cycles===TEMPLATE(main-cycles,10000)");
            put("main-update", "run driver=cql tags=block:main-update threads=auto cycles===TEMPLATE(main-cycles,10000)");

            put("truncate", "run driver=cql tags=block:truncate.* threads===UNDEF cycles===UNDEF");
            put("schema-keyspaces", "run driver=cql tags=block:schema-keyspaces threads===UNDEF cycles===UNDEF");
            put("schema-types", "run driver=cql tags=block:schema-types threads===UNDEF cycles===UNDEF");
            put("schema-tables", "run driver=cql tags=block:schema-tables threads===UNDEF cycles===UNDEF");
            put("drop", "run driver=cql tags=block:drop.* threads===UNDEF cycles===UNDEF");
            put("drop-tables", "run driver=cql tags=block:drop-tables threads===UNDEF cycles===UNDEF");
            put("drop-types", "run driver=cql tags=block:drop-types threads===UNDEF cycles===UNDEF");
            put("drop-keyspaces", "run driver=cql tags=block:drop-keyspaces threads===UNDEF cycles===UNDEF");

        }};
    }

    private Map<String, Object> genScanOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> blockdata = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        blockdata.put("ops", ops);
        for (CqlTable table : model.getTableDefs()) {
            if (table.getClusteringColumns().size() == 0) {
                logger.debug(() -> "skipping table " + table.getFullName() + " for scan since there are no clustering columns");
            }
            ops.put(
                    namer.nameFor(table, "optype", "scan", "blockname", blockname),
                    Map.of(
                            "prepared", genScanSyntax(table),
                            "timeout", timeouts.get("scan"),
                            "ratio", readRatioFor(table)
                    )
            );
        }
        return blockdata;
    }

    private String genScanSyntax(CqlTable table) {
        return """
                select * from KEYSPACE.TABLE
                where PREDICATE
                LIMIT;
                """
                .replace("KEYSPACE", table.getKeyspace().getName())
                .replace("TABLE", table.getName())
                .replace("PREDICATE", genPredicateTemplate(table, -1))
                .replace("LIMIT", genLimitSyntax(table));
    }


    private Map<String, Object> genSelectOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> blockdata = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        blockdata.put("ops", ops);
        for (CqlTable table : model.getTableDefs()) {
            ops.put(
                    namer.nameFor(table, "optype", "select", "blockname", blockname),
                    Map.of(
                            "prepared", genSelectSyntax(table),
                            "timeout", timeouts.get("select"),
                            "ratio", readRatioFor(table)
                    )
            );
        }
        return blockdata;
    }

    private String genSelectSyntax(CqlTable table) {
        return """
                select * from  KEYSPACE.TABLE
                where PREDICATE
                LIMIT;
                """
                .replace("KEYSPACE", table.getKeyspace().getName())
                .replace("TABLE", table.getName())
                .replace("PREDICATE", genPredicateTemplate(table, 0))
                .replace("LIMIT", genLimitSyntax(table));
    }

    private String genLimitSyntax(CqlTable table) {
        return " LIMIT 10";
    }

    private Map<String, Object> genInsertOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> blockdata = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        blockdata.put("ops", ops);
        for (CqlTable table : model.getTableDefs()) {
            if (!isCounterTable(table)) {
                ops.put(
                        namer.nameFor(table, "optype", "insert", "blockname", blockname),
                        Map.of(
                                "prepared", genInsertSyntax(table),
                                "timeout", timeouts.get("insert"),
                                "ratio", writeRatioFor(table)
                        )
                );
            }
        }
        return blockdata;
    }

    private String genInsertSyntax(CqlTable table) {
        if (isCounterTable(table)) {
            logger.warn("skipping insert on counter table '" + table.getFullName());
        }

        return """
                insert into KEYSPACE.TABLE
                ( FIELDNAMES )
                VALUES
                ( BINDINGS );
                """
                .replace("KEYSPACE", table.getKeyspace().getName())
                .replace("TABLE", table.getName())
                .replace("FIELDNAMES",
                        String.join(", ",
                                table.getColumnDefs().stream()
                                        .map(CqlTableColumn::getName).toList()))
                .replaceAll("BINDINGS",
                        String.join(", ",
                                table.getColumnDefs().stream()
                                        .map(c -> binder.forColumn(c))
                                        .map(c -> "{" + c.getName() + "}").toList()));
    }


    private Map<String, Object> genUpdateOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> blockdata = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        blockdata.put("ops", ops);
        for (CqlTable table : model.getTableDefs()) {
            ops.put(
                    namer.nameFor(table, "optype", "update", "blockname", blockname),
                    Map.of(
                            "prepared", genUpdateSyntax(table),
                            "timeout", timeouts.get("update"),
                            "ratio", writeRatioFor(table)
                    )
            );
        }
        return blockdata;
    }


    private boolean isCounterTable(CqlTable table) {
        return table.getColumnDefs().stream()
                .anyMatch(cd -> cd.getTrimmedTypedef().equalsIgnoreCase("counter"));
    }

    private int totalRatioFor(CqlTable table) {
        if (table.hasStats()) {
            return readRatioFor(table) + writeRatioFor(table);
        } else {
            return 1;
        }
    }

    private int readRatioFor(CqlTable table) {
        if (table.getTableAttributes() == null || table.getTableAttributes().size() == 0) {
            return 1;
        }
        double weighted_reads = table.getComputedStats().getWeightedReadsOfTotal();
        return (int) (weighted_reads * DEFAULT_RESOLUTION);
    }

    private int writeRatioFor(CqlTable table) {
        if (table.getTableAttributes() == null || table.getTableAttributes().size() == 0) {
            return 1;
        }
        double weighted_writes = table.getComputedStats().getWeightedWritesOfTotal();
        return (int) (weighted_writes * DEFAULT_RESOLUTION);
    }

    /**
     * If keycount is 0, all key fields including partition and clustering fields
     * are qualfied with predicates.
     * If keycount is positive, then only that many will be included.
     * If keycount is negative, then that many keyfields will be removed from the
     * predicate starting with the rightmost (innermost) fields first.
     *
     * @param table
     * @param keycount
     * @return
     */
    private String genPredicateTemplate(CqlTable table, int keycount) {

        StringBuilder sb = new StringBuilder();
        LinkedList<CqlTableColumn> pkeys = new LinkedList<>();
        for (String pkey : table.getPartitionKeys()) {
            CqlTableColumn coldef = table.getColumnDefForName(pkey);
            pkeys.push(coldef);
        }
        for (String ccol : table.getClusteringColumns()) {
            CqlTableColumn coldef = table.getColumnDefForName(ccol);
            pkeys.push(coldef);
        }

        if (keycount > 0) {
            while (pkeys.size() > keycount) {
                pkeys.pop();
            }
        } else if (keycount < 0) {
            for (int i = 0; i > keycount; i--) {
                pkeys.pop();
            }
        }
        var lastcount = keycount;
        keycount = Math.max(table.getPartitionKeys().size(), keycount);
        if (keycount != lastcount) {
            logger.debug("minimum keycount for " + table.getFullName() + " adjusted from " + lastcount + " to " + keycount);
        }

        // TODO; constraints on predicates based on valid constructions
        pkeys.stream().map(this::genPredicatePart)
                .forEach(p -> {
                    sb.append(p).append("\n  AND ");
                });
        if (sb.length() > 0) {
            sb.setLength(sb.length() - "\n  AND ".length());
        }
        return sb.toString();
    }

    private String genPredicatePart(CqlTableColumn def) {
        String typeName = def.getTrimmedTypedef();
        Binding binding = binder.forColumn(def);
        return def.getName() + "={" + binding.getName() + "}";
    }

    private String genUpdateSyntax(CqlTable table) {
        return """
                update KEYSPACE.TABLE
                set ASSIGNMENTS
                where PREDICATES;
                """
                .replaceAll("KEYSPACE", table.getKeyspace().getName())
                .replaceAll("TABLE", table.getName())
                .replaceAll("PREDICATES", genPredicateTemplate(table, 0))
                .replaceAll("ASSIGNMENTS", genAssignments(table));
    }

    private String genAssignments(CqlTable table) {
        StringBuilder sb = new StringBuilder();
        for (CqlTableColumn coldef : table.getNonKeyColumnDefinitions()) {
            if (coldef.isCounter()) {
                sb.append(coldef.getName()).append("=")
                        .append(coldef.getName()).append("+").append("{").append(binder.forColumn(coldef).getName()).append("}")
                        .append(", ");
            } else {
                sb.append(coldef.getName()).append("=")
                        .append("{").append(binder.forColumn(coldef).getName()).append("}")
                        .append(", ");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - ", ".length());
        }
        return sb.toString();
    }


    public String getWorkloadAsYaml() {

        if (model.isEmpty()) {
            throw new RuntimeException("Can't build a workload yaml with no elements to process. The parsed model is empty. Did you filter everything out?");
        }
        this.namer = new NamingFolio(this.namingTemplate);
        BindingsLibrary defaultBindings = new CGDefaultCqlBindings();
        BindingsAccumulator bindingslib = new BindingsAccumulator(namer, List.of(defaultBindings));
        this.binder = new CGColumnRebinder(bindingslib, 10, 1);
        namer.informNamerOfAllKnownNames(model);

        Map<String, Object> workload = generateBlocks();
        ((Map<String, String>) workload.get("bindings")).putAll(bindingslib.getAccumulatedBindings());

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


        return dump.dumpToString(workload);
    }

    public String getModelAsJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(model);
    }

    public String getWorkoadAsJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> workload = generateBlocks();
        return gson.toJson(workload);
    }


    private Map<String, Object> genDropTablesBlock(CqlModel model, String blockname) {
        Map<String, Object> dropTablesBlock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        dropTablesBlock.put("ops", ops);
        for (CqlTable table : model.getTableDefs()) {
            ops.put(
                    namer.nameFor(table, "optype", "drop", "blockname", blockname),
                    Map.of(
                            "simple", "drop table if exists " + table.getFullName() + ";",
                            "timeout", timeouts.get("drop")
                    )
            );
        }
        return dropTablesBlock;
    }

    private Map<String, Object> genDropTypesBlock(CqlModel model, String blockname) {
        Map<String, Object> dropTypesBlock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        dropTypesBlock.put("ops", ops);
        for (CqlType type : model.getTypeDefs()) {
            ops.put(
                    namer.nameFor(type, "optype", "drop-type", "blockname", blockname),
                    Map.of(
                            "simple", "drop type if exists " + type.getKeyspace() + "." + type.getName() + ";",
                            "timeout", timeouts.get("drop")
                    )
            );
        }
        return dropTypesBlock;
    }

    private Map<String, Object> genDropKeyspacesOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> dropTypesBlock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        dropTypesBlock.put("ops", ops);
        for (CqlType type : model.getTypeDefs()) {
            ops.put(
                    namer.nameFor(type, "optype", "drop-keyspace", "blockname", blockname),
                    Map.of(
                            "simple", "drop keyspace if exists " + type.getKeyspace() + ";",
                            "timeout", timeouts.get("drop")
                    )
            );
        }
        return dropTypesBlock;
    }


    private Map<String, Object> genTruncateTablesOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> truncateblock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        truncateblock.put("ops", ops);

        for (CqlTable table : model.getTableDefs()) {
            ops.put(
                    namer.nameFor(table, "optype", "truncate", "blockname", blockname),
                    Map.of(
                            "simple", "truncate " + table.getFullName() + ";",
                            "timeout", timeouts.get("truncate")
                    )
            );
        }
        return truncateblock;
    }

    private Map<String, Object> genCreateKeyspacesOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> schemablock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();

        for (CqlKeyspaceDef ks : model.getKeyspaceDefs()) {
            ops.put(
                    namer.nameFor(ks, "optype", "create", "blockname", blockname),
                    Map.of(
                            "simple", genKeyspaceDDL(ks),
                            "timeout", timeouts.get("create")
                    )
            );
        }

        schemablock.put("ops", ops);
        return schemablock;
    }

    private Map<String, Object> genCreateTypesOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> blockdata = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();
        blockdata.put("ops", ops);

        model.getTypeDefs().forEach(type -> {
            ops.put(
                    namer.nameFor(type, "optype", "create", "blockname", blockname),
                    Map.of(
                            "simple", genTypeDDL(type),
                            "timeout", timeouts.get("create")
                    )
            );
        });

        return blockdata;

    }

    private String genKeyspaceDDL(CqlKeyspaceDef keyspace) {
        return """
                create keyspace KEYSPACE
                with replication = {REPLICATION}DURABLEWRITES?;
                """
                .replace("KEYSPACE", keyspace.getName())
                .replace("REPLICATION", keyspace.getReplicationData())
                .replace("DURABLEWRITES?", keyspace.isDurableWrites() ? "" : "\n and durable writes = false")
                ;
    }

    private Map<String, Object> genCreateTablesOpTemplates(CqlModel model, String blockname) {
        Map<String, Object> schemablock = new LinkedHashMap<>();
        Map<String, Object> ops = new LinkedHashMap<>();

        model.getTableDefs().forEach(table -> {
            ops.put(
                    namer.nameFor(table, "optype", "create", "blockname", blockname),
                    Map.of(
                            "simple", genTableDDL(table),
                            "timeout", timeouts.get("create")
                    )
            );
        });

        schemablock.put("ops", ops);
        return schemablock;
    }


    private String genTypeDDL(CqlType type) {
        return """
                create type KEYSPACE.TYPENAME (
                TYPEDEF
                );
                """
                .replace("KEYSPACE", type.getKeyspace().getName())
                .replace("TYPENAME", type.getName())
                .replace("TYPEDEF", type.getColumnDefs().stream()
                        .map(def -> def.getName() + " " + def.getTypedef()).collect(Collectors.joining(",\n")));
    }

    private Object genTableDDL(CqlTable cqltable) {
        if (cqltable.isCompactStorage()) {
            logger.warn("COMPACT STORAGE is not supported, eliding this option for table '" + cqltable.getFullName() + "'");
        }

        return """
                create table if not exists KEYSPACE.TABLE (
                COLUMN_DEFS,
                primary key (PRIMARYKEY)
                )CLUSTERING;
                """
                .replace("KEYSPACE", cqltable.getKeyspace().getName())
                .replace("TABLE", cqltable.getName())
                .replace("COLUMN_DEFS", genTableColumnDDL(cqltable))
                .replace("PRIMARYKEY", genPrimaryKeyDDL(cqltable))
                .replace("CLUSTERING", genTableClusteringOrderDDL(cqltable));

    }

    private String genPrimaryKeyDDL(CqlTable cqltable) {
        StringBuilder sb = new StringBuilder("(");
        for (String partitionKey : cqltable.getPartitionKeys()) {
            sb.append(partitionKey).append(", ");
        }
        sb.setLength(sb.length() - ", ".length());
        sb.append(")");
        for (String clusteringColumn : cqltable.getClusteringColumns()) {
            sb.append(", ").append(clusteringColumn);
        }
        return sb.toString();
    }

    private String genTableClusteringOrderDDL(CqlTable cqltable) {
        if (cqltable.getClusteringOrders().size() == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(" with clustering order by (\n");
            for (int i = 0; i < cqltable.getClusteringOrders().size(); i++) {
                sb.append(cqltable.getClusteringColumns().get(i));
                sb.append(" ");
                sb.append(cqltable.getClusteringOrders().get(i));
                sb.append(",\n");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - ",\n".length());
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private String genTableColumnDDL(CqlTable cqltable) {
        return cqltable.getColumnDefs().stream()
                .map(cd -> cd.getName() + " " + cd.getTrimmedTypedef())
                .collect(Collectors.joining(",\n"));
    }


}
