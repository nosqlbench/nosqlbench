package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.oss.driver.api.core.CqlSession;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class Cqld4FluentGraphOpMapper implements OpMapper<Op>  {
    private final CqlSession session;

    public Cqld4FluentGraphOpMapper(CqlSession session) {
        this.session = session;
   }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp cmd) {
        GraphTraversalSource g = DseGraph.g;

        ParsedTemplate fluent = cmd.getAsTemplate("fluent").orElseThrow();
        String scriptBodyWithRawVarRefs = fluent.getPositionalStatement();
        Supplier<Script> supplier = () -> {
            groovy.lang.Binding groovyBindings = new Binding(new LinkedHashMap<String,Object>(Map.of("g",g)));
            GroovyShell gshell = new GroovyShell(groovyBindings);
            return gshell.parse(scriptBodyWithRawVarRefs);
        };

        LongFunction<? extends String> graphnameFunc = cmd.getAsRequiredFunction("graphname");
        Bindings virtdataBindings = new BindingsTemplate(fluent.getBindPoints()).resolveBindings();
//        Map<String, Object> values = virtdataBindings.getAllMap(1L);
//        values.forEach(groovyBindings::setVariable);
//        GraphTraversal<Vertex,Vertex> v = (GraphTraversal<Vertex, Vertex>) parsed.run();
//        FluentGraphStatement fgs = new FluentGraphStatementBuilder(v).setGraphName("graph_wheels").build();

        return new Cqld4FluentGraphOpDispenser(cmd, graphnameFunc, session, virtdataBindings, supplier);
    }
}
