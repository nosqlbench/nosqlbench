package com.datastax.ebdrivers.dsegraph.statements;

import com.datastax.driver.dse.graph.SimpleGraphStatement;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.bindings.ContextualBindingsTemplate;
import io.nosqlbench.virtdata.core.bindings.ValuesBinder;
import io.nosqlbench.virtdata.core.templates.BindPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadyGraphStatementTemplate {

    private final ContextualBindingsTemplate<SimpleGraphStatement, SimpleGraphStatement> contextualBindingsTemplate;
    private final String name;
    private String[] fields;

    public ReadyGraphStatementTemplate(String name, String stmtTemplate, List<BindPoint> bindPoints, String[] fields) {
        this.name = name;
        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement(stmtTemplate);
        BindingsTemplate bindingsTemplate = new BindingsTemplate(bindPoints);
        contextualBindingsTemplate = new ContextualBindingsTemplate<>(
                simpleGraphStatement, bindingsTemplate,
                new ParameterizedGraphStatementValuesBinder(fields)
        );
    }

    public ReadyGraphStatementTemplate(String name, String stmtTemplate, List<BindPoint> bindPoints, String[] fields, int repeat) {
        this.name = name;
        SimpleGraphStatement simpleGraphStatement = new SimpleGraphStatement(stmtTemplate);
        BindingsTemplate bindingsTemplate = new BindingsTemplate(bindPoints);

        contextualBindingsTemplate = new ContextualBindingsTemplate<>(simpleGraphStatement, bindingsTemplate, new ParameterizedIteratedGraphStatementValuesBinder(fields, repeat));
    }

    public static class ParameterizedIteratedGraphStatementValuesBinder implements ValuesBinder<SimpleGraphStatement, SimpleGraphStatement> {

        private final String[] fields;
        private final int repeat;

        public ParameterizedIteratedGraphStatementValuesBinder(String[] fields, int repeat) {
            this.fields = fields;
            this.repeat = repeat;
        }

        @Override
        public SimpleGraphStatement bindValues(SimpleGraphStatement context, Bindings bindings, long cycle) {
            Map<String, Object> iteratedSuffixMap = bindings.getIteratedSuffixMap(cycle, repeat, fields);
            return new SimpleGraphStatement(context.getQueryString(), iteratedSuffixMap);
        }
    }

    public static class ParameterizedGraphStatementValuesBinder
            implements ValuesBinder<SimpleGraphStatement, SimpleGraphStatement> {
        private final String[] fields;
        private final Map valuesMap = new HashMap();
        private final ThreadLocal<Map<String, Object>> mapTL;

        public ParameterizedGraphStatementValuesBinder(String[] fields) {
            this.fields = fields;
            for (String field : fields) {
                valuesMap.put(field, null);
            }
            mapTL = ThreadLocal.withInitial(() -> new HashMap<String, Object>(valuesMap));
        }

        @Override
        public SimpleGraphStatement bindValues(SimpleGraphStatement context, Bindings bindings, long cycle) {
            bindings.updateMap(mapTL.get(), cycle);
            return new SimpleGraphStatement(context.getQueryString(), mapTL.get());
        }
    }

    public ReadyGraphStatement resolve() {
        return new ReadyGraphStatement(contextualBindingsTemplate.resolveBindings());
    }
}