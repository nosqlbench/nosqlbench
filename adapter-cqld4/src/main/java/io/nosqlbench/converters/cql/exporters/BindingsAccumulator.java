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
    private final Map<String,String> accumulated = new LinkedHashMap<>();

    public BindingsAccumulator(NamingFolio namer, List<BindingsLibrary> libraries) {
        this.namer = namer;
        this.libraries = libraries;
    }

    public Binding forColumn(CqlColumnDef def, String... extra) {
        String name = namer.nameFor(def, extra);
        for (BindingsLibrary library : libraries) {
            Optional<String> bindingRecipe = library.resolveBindingsFor(def);
            if (bindingRecipe.isPresent()) {
                Binding newBinding = new Binding(name, bindingRecipe.get());
                registerBinding(newBinding);
                return newBinding;
            }
        }
        logger.error("Unable to find a binding for column def '" + def + "', installing 'TBD' place-holder until this is supported. This workload will not be functional until then.");
        return new Binding(name, "TBD");
//        throw new RuntimeException("Unable to find a binding for column def '" + def + "'");
    }

    private void registerBinding(Binding newBinding) {
        accumulated.put(newBinding.name(), newBinding.recipe());
    }

    public Map<String,String> getAccumulatedBindings() {
        return accumulated;
    }
}
