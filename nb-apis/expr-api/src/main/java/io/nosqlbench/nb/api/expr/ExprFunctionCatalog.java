package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Provides a cached catalog of expression functions and their metadata.
 */
public final class ExprFunctionCatalog {

    private ExprFunctionCatalog() {
    }

    private static class Holder {
        private static final Map<String, ExprFunctionMetadata> METADATA = load();

        private static Map<String, ExprFunctionMetadata> load() {
            Map<String, ExprFunctionMetadata> byName = new LinkedHashMap<>();
            ServiceLoader.load(ExprFunctionProvider.class).forEach(provider -> {
                // First, extract metadata from annotations on the provider class itself
                ExprFunctionAnnotations.extractMetadata(provider.getClass()).forEach(metadata -> {
                    ExprFunctionMetadata finalMeta = Objects.requireNonNull(metadata, "metadata");
                    byName.put(finalMeta.name(), finalMeta);
                });

                // For GroovyLibraryAutoLoader, also load library function metadata
                if (provider instanceof GroovyLibraryAutoLoader loader) {
                    // Initialize the loader to load library scripts and their metadata
                    groovy.lang.Binding binding = new groovy.lang.Binding();
                    org.codehaus.groovy.control.CompilerConfiguration config = new org.codehaus.groovy.control.CompilerConfiguration();
                    groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell(binding, config);
                    loader.loadLibrariesWithShell(shell);

                    // Add library metadata to the catalog
                    loader.getLibraryMetadata().forEach((name, metadata) -> {
                        byName.put(name, metadata);
                    });
                }
            });
            return byName;
        }
    }

    public static List<ExprFunctionMetadata> listMetadata() {
        return Holder.METADATA.values().stream()
            .sorted(Comparator.comparing(ExprFunctionMetadata::name))
            .toList();
    }

    public static ExprFunctionMetadata get(String name) {
        return Holder.METADATA.get(name);
    }
}
