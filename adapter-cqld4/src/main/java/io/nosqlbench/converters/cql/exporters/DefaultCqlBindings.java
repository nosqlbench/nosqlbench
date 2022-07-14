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

import io.nosqlbench.converters.cql.cqlast.CqlColumnDef;
import io.nosqlbench.converters.cql.exporters.binders.Binding;
import io.nosqlbench.converters.cql.exporters.binders.BindingsLibrary;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class DefaultCqlBindings implements BindingsLibrary {

    private final Map<String, String> bindings;

    public DefaultCqlBindings() {
        String yamlContent = NBIO.all()
            .name("bindings")
            .extension("yaml", "yml")
            .first()
            .map(Content::asString)
            .or(() -> loadLocal("bindings.yaml"))
            .orElseThrow(() -> new RuntimeException("Unable to load bindings.yaml"));
        StmtsDocList stmtsDocs = StatementsLoader.loadString(yamlContent, Map.of());
        this.bindings = stmtsDocs.getDocBindings();
    }

    private Optional<String> loadLocal(String path) {
        try {
            String resourceName = getClass().getPackageName().replaceAll("\\.", File.separator)+File.separator+path;
            InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
            byte[] bytes = stream.readAllBytes();
            return Optional.of(new String(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Binding> resolveBindingsFor(CqlColumnDef def) {
        String typedef = def.getTrimmedTypedef();
        String recipe = bindings.get(def.getTrimmedTypedef());
        Binding optionalBinding = new Binding(typedef, recipe);
        return Optional.ofNullable(optionalBinding);
    }
}
