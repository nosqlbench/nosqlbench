package io.nosqlbench.converters.cql.cql.exporters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.converters.cql.cql.cqlast.CqlColumnDef;
import io.nosqlbench.converters.cql.cql.cqlast.CqlKeyspace;
import io.nosqlbench.converters.cql.cql.cqlast.CqlModel;
import io.nosqlbench.converters.cql.cql.cqlast.CqlTable;
import io.nosqlbench.converters.cql.cql.parser.CqlModelParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CqlWorkloadExporter {

    private final static Logger logger = LogManager.getLogger(CqlWorkloadExporter.class);

    private final CqlModel model;

    public CqlWorkloadExporter(CqlModel model) {
        this.model = model;
    }

    public CqlWorkloadExporter(String ddl) {
        this.model = CqlModelParser.parse(ddl);
    }

    public CqlWorkloadExporter(Path path) {
        this.model = CqlModelParser.parse(path);
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
                    t -> "insert-" + t.getTableName(),
                    this::genUpsertTemplate)
                )
        );

        mainOpTemplates.putAll(
            model.getAllTables()
                .stream()
                .collect(Collectors.toMap(
                    t -> "select-" + t.getTableName(),
                    this::genSelectTemplate)
                ));


        return Map.of("ops", mainOpTemplates);
    }


    private Map<String, Object> genRampupBlock(CqlModel model) {
        Map<String, String> rampupOpTemplates = model.getAllTables()
            .stream()
            .collect(Collectors.toMap(
                t -> "insert-" + t.getTableName(),
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
        CqlLiteralFormat cqlLiteralFormat = null;
        try {
            cqlLiteralFormat = CqlLiteralFormat.valueOf(typeName);
        } catch (IllegalArgumentException iae) {
            cqlLiteralFormat = CqlLiteralFormat.UNKNOWN;
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
