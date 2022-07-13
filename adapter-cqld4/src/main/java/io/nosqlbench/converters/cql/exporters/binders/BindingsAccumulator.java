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

package io.nosqlbench.converters.cql.exporters.binders;

import io.nosqlbench.converters.cql.cqlast.CqlColumnDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BindingsAccumulator {
    private final static Logger logger = LogManager.getLogger("CQL-GENERATOR");

    private final NamingFolio namer;
    private final List<BindingsLibrary> libraries;
    private final Map<String, String> accumulated = new LinkedHashMap<>();
    private final NamingStyle namingStyle = NamingStyle.SymbolicType;
    LinkedHashMap<String,Integer> counts = new LinkedHashMap<>();

    public BindingsAccumulator(NamingFolio namer, List<BindingsLibrary> libraries) {
        this.namer = namer;
        this.libraries = libraries;
    }

    public Binding forColumn(CqlColumnDef def, String... extra) {
        for (BindingsLibrary library : libraries) {
            Optional<Binding> binding = switch (namingStyle) {
                case FullyQualified -> this.resolveFullyQualifiedBinding(def, extra);
                case SymbolicType -> this.resolveSymbolicBinding(def, extra);
                case CondensedKeyspace -> this.resolvedCondensedBinding(def, extra);
            };
            if (binding.isPresent()) {
                registerBinding(binding.get());
                return binding.get();
            }
        }
        throw new UnresolvedBindingException(def);
    }

    private Optional<Binding> resolvedCondensedBinding(CqlColumnDef def, String[] extra) {
        throw new RuntimeException("Implement me!");
    }

    private Optional<Binding> resolveSymbolicBinding(CqlColumnDef def, String[] extra) {
        for (BindingsLibrary library : libraries) {
            Optional<Binding> binding = library.resolveBindingsFor(def);
            if (binding.isPresent()) {
                return binding;
            }
        }
        return Optional.empty();

    }

    private Optional<Binding> resolveFullyQualifiedBinding(CqlColumnDef def, String[] extra) {
        for (BindingsLibrary library : libraries) {
            Optional<Binding> bindingRecipe = library.resolveBindingsFor(def);
            if (bindingRecipe.isPresent()) {
                Binding found = bindingRecipe.get();
                String name = namer.nameFor(def, extra);
                Binding renamedBinding = new Binding(name,found.recipe());
                return Optional.of(renamedBinding);
            }
        }
        return Optional.empty();
    }

    private void registerBinding(Binding newBinding) {
        String name = newBinding.name();
        accumulated.put(name, newBinding.recipe());
        counts.put(name, counts.get(name)==null? 1 : counts.get(name)+1);
    }

    public Map<String, String> getAccumulatedBindings() {
        return accumulated;
    }
}
