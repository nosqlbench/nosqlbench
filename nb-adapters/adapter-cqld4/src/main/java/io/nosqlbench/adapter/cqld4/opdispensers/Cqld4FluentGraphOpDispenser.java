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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatementBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import groovy.lang.Script;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4FluentGraphOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class Cqld4FluentGraphOpDispenser extends Cqld4BaseOpDispenser<Cqld4FluentGraphOp> {

    private final LongFunction<? extends String> graphnameFunc;
    private final Bindings virtdataBindings;
    private final ThreadLocal<Script> tlScript;

    public Cqld4FluentGraphOpDispenser(
        Cqld4DriverAdapter adapter,
        ParsedOp op,
        LongFunction<? extends String> graphnameFunc,
        Bindings virtdataBindings,
        Supplier<Script> scriptSupplier
    ) {
        super(adapter, op);
        this.graphnameFunc = graphnameFunc;
        this.virtdataBindings = virtdataBindings;
        this.tlScript = ThreadLocal.withInitial(scriptSupplier);
    }

    @Override
    public Cqld4FluentGraphOp getOp(long cycle) {
        String graphname = graphnameFunc.apply(cycle);
        Script script = tlScript.get();
        Map<String, Object> allMap = virtdataBindings.getAllMap(cycle);
        allMap.forEach((k,v) -> script.getBinding().setVariable(k,v));
        GraphTraversal<Vertex,Vertex> v = (GraphTraversal<Vertex, Vertex>) script.run();
        FluentGraphStatement fgs = new FluentGraphStatementBuilder(v).setGraphName(graphname).build();
        return new Cqld4FluentGraphOp(sessionF.apply(cycle),fgs);
    }



}
