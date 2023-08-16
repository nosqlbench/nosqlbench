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

package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.oss.driver.api.core.CqlSession;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4FluentGraphOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class Cqld4FluentGraphOpMapper implements OpMapper<Op> {
    private final static Logger logger = LogManager.getLogger(Cqld4FluentGraphOpMapper.class);

    private final LongFunction<CqlSession> sessionFunc;
    private final TypeAndTarget<CqlD4OpType, String> target;
    private final DriverAdapter adapter;
    private GraphTraversalSource gtsPlaceHolder;

    public Cqld4FluentGraphOpMapper(DriverAdapter adapter, LongFunction<CqlSession> sessionFunc, TypeAndTarget<CqlD4OpType, String> target) {
        this.sessionFunc = sessionFunc;
        this.target = target;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {
        GraphTraversalSource g = DseGraph.g;

        ParsedTemplateString fluent = op.getAsTemplate(target.field).orElseThrow();
        String scriptBodyWithRawVarRefs = fluent.getPositionalStatement();

        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

        if (op.isDynamic("imports")) {
            throw new OpConfigError("You may only define imports as a static list. Dynamic values are not allowed.");
        }

        List imports = op.getOptionalStaticValue("imports", List.class)
            .orElse(List.of("org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__"));
        String[] verifiedClasses = expandClassNames(imports);
        ImportCustomizer importer = new ImportCustomizer();
        importer.addImports(verifiedClasses);
        compilerConfiguration.addCompilationCustomizers(importer);

        Supplier<Script> supplier = () -> {
            groovy.lang.Binding groovyBindings = new Binding(new LinkedHashMap<String, Object>(Map.of("g", g)));
            GroovyShell gshell = new GroovyShell(groovyBindings, compilerConfiguration);
            return gshell.parse(scriptBodyWithRawVarRefs);
        };

        LongFunction<? extends String> graphnameFunc = op.getAsRequiredFunction("graphname");
        Bindings virtdataBindings = new BindingsTemplate(fluent.getBindPoints()).resolveBindings();

        return new Cqld4FluentGraphOpDispenser(adapter, op, graphnameFunc, sessionFunc, virtdataBindings, supplier);
    }

    private String[] expandClassNames(List l) {
        ClassLoader loader = Cqld4FluentGraphOpMapper.class.getClassLoader();

        List<String> classNames = new ArrayList<>();
        for (Object name : l) {
            String candidateName = name.toString();
            if (candidateName.endsWith(".*")) {
                throw new RuntimeException("You can not use wildcard package imports like '" + candidateName + "'");
            }
            try {
                loader.loadClass(candidateName);
                classNames.add(candidateName);
                logger.debug(() -> "added import " + candidateName);
            } catch (Exception e) {
                throw new RuntimeException("Class '" + candidateName + "' was not found for fluent imports.");
            }
        }
        return classNames.toArray(new String[0]);
    }
}
