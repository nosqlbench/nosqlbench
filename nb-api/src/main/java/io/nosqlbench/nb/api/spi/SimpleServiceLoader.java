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

package io.nosqlbench.nb.api.spi;

import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleServiceLoader<T> {

    private static final Logger logger = LogManager.getLogger("SERVICESAPI");
    private final Class<? extends T> serviceType;
    private Maturity maturity = Maturity.Unspecified;

    public SimpleServiceLoader(Class<? extends T> serviceType, Maturity maturity) {
        this.serviceType = serviceType;
        this.maturity = maturity;
    }

    public SimpleServiceLoader setMaturity(Maturity maturity) {
        this.maturity = maturity;
        return this;
    }

    public Optional<T> get(String implName) {
        List<Component<? extends T>> namedProviders = getNamedProviders();
        if (namedProviders == null) {
            return Optional.empty();
        }
        List<Component<? extends T>> components = namedProviders.stream().filter(n -> n.selector.equals(implName)).toList();
        if (components.size() > 1) {
            throw new RuntimeException("Found multiple components matching '" + implName + "',");
        }
        if (components.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(components.get(0).provider.get());
    }

    public T getOrThrow(String implName) {
        Optional<T> t = get(implName);
        return t.orElseThrow(
            () -> new RuntimeException(serviceType.getSimpleName() + " '" + implName + "' not found. Available types:" + getNamedProviders())
        );
    }


    public Optional<T> getOptionally(String implName) {
        Optional<T> type = get(implName);
        return type;
    }

    /**
     * Load the service providers which are annotated with {@link Service} and selector names.
     *
     * @param includes If provided, a list of patterns which are used to include
     *                 named services based on the selector name from the
     *                 {@link Service} annotation.
     * @return A map of providers of T
     */
    public synchronized List<Component<? extends T>> getNamedProviders(String... includes) {
        ServiceLoader<? extends T> loader = ServiceLoader.load(serviceType);
        List<String> defaultedPatterns = (includes != null && includes.length > 0) ? Arrays.asList(includes) : List.of(".*");
        List<Pattern> qualifiedPatterns = defaultedPatterns.stream()
            .map(Pattern::compile).collect(Collectors.toList());

        List<Component<? extends T>> components = new ArrayList<>();

        loader.stream().forEach(provider -> {
            logger.trace("loading provider: " + provider.type());
            Class<? extends T> type = provider.type();
            if (!type.isAnnotationPresent(Service.class)) {
                throw new RuntimeException(
                    "Annotator services must be annotated with distinct selectors\n" +
                        "such as @Service(Annotator.class,selector=\"myimpl42\")"
                );
            }
            Service service = type.getAnnotation(Service.class);
            for (Pattern pattern : qualifiedPatterns) {
                if (pattern.matcher(service.selector()).matches()) {
                    components.add(new Component(service.selector(), provider, service.maturity()));
                    break;
                }
            }
        });

        return components;
    }


    public final static class Component<T> {

        public final String selector;
        public final ServiceLoader.Provider<? extends T> provider;
        public final Maturity maturity;

        public Component(String selector, ServiceLoader.Provider<? extends T> provider, Maturity maturity) {
            this.selector = selector;
            this.provider = provider;
            this.maturity = maturity;
        }

        @Override
        public String toString() {
            return "Component{" +
                selector + " (" + maturity + ")";
        }
    }


    public Map<String, Maturity> getAllSelectors(String... patterns) {
        LinkedHashMap<String, Maturity> map = new LinkedHashMap<>();
        for (Component<? extends T> namedProvider : getNamedProviders(patterns)) {
            map.put(namedProvider.selector, namedProvider.maturity);
        }
        return map;
    }
}
