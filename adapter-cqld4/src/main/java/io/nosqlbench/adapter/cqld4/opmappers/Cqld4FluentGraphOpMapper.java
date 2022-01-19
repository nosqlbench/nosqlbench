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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.LinkedHashMap;
import java.util.List;
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

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

        List<String> imports = cmd.getStaticConfigOr("imports", List.of());
        ImportCustomizer importer = new ImportCustomizer();
        importer.addImports(imports.toArray(new String[0]));
        compilerConfiguration.addCompilationCustomizers(importer);

        Supplier<Script> supplier = () -> {
            groovy.lang.Binding groovyBindings = new Binding(new LinkedHashMap<String,Object>(Map.of("g",g)));
            GroovyShell gshell = new GroovyShell(groovyBindings,compilerConfiguration);
            return gshell.parse(scriptBodyWithRawVarRefs);
        };

        LongFunction<? extends String> graphnameFunc = cmd.getAsRequiredFunction("graphname");
        Bindings virtdataBindings = new BindingsTemplate(fluent.getBindPoints()).resolveBindings();

        return new Cqld4FluentGraphOpDispenser(cmd, graphnameFunc, session, virtdataBindings, supplier);
    }
}
