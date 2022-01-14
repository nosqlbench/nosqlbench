package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatementBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import groovy.lang.Script;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4FluentGraphOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class Cqld4FluentGraphOpDispenser extends BaseOpDispenser<Op> {

    private final LongFunction<? extends String> graphnameFunc;
    private final CqlSession session;
    private final Bindings virtdataBindings;
    private final ThreadLocal<Script> tlScript;

    public Cqld4FluentGraphOpDispenser(
        ParsedOp optpl,
        LongFunction<? extends String> graphnameFunc,
        CqlSession session,
        Bindings virtdataBindings,
        Supplier<Script> scriptSource
    ) {
        super(optpl);
        this.graphnameFunc = graphnameFunc;
        this.session = session;
        this.virtdataBindings = virtdataBindings;
        this.tlScript = ThreadLocal.withInitial(scriptSource);
    }

    @Override
    public Op apply(long value) {
        String graphname = graphnameFunc.apply(value);
        Script script = tlScript.get();
        Map<String, Object> allMap = virtdataBindings.getAllMap(value);
        allMap.forEach((k,v) -> script.getBinding().setVariable(k,v));
        GraphTraversal<Vertex,Vertex> v = (GraphTraversal<Vertex, Vertex>) script.run();
        FluentGraphStatement fgs = new FluentGraphStatementBuilder(v).setGraphName(graphname).build();
        return new Cqld4FluentGraphOp(session,fgs);
    }



}
