/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.nb.api.spi;

import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleServiceLoader<T> {

    private static final Logger logger = LogManager.getLogger("SERVICESAPI");
    private final Class<? extends T> serviceType;

    public SimpleServiceLoader(Class<? extends T> serviceType) {
        this.serviceType = serviceType;
    }

    public Optional<T> get(String implName) {
        LinkedHashMap<String, ServiceLoader.Provider<? extends T>> namedProviders = getNamedProviders();
        ServiceLoader.Provider<? extends T> providers = namedProviders.get(implName);
        return Optional.ofNullable(providers == null ? null : providers.get());
    }

    public T getOrThrow(String implName) {
        Optional<T> t = get(implName);
        return t.orElseThrow(
            () -> new RuntimeException(serviceType.getSimpleName() + " '" + implName + "' not found. Available types:" +
                this.getNamedProviders().keySet().stream().collect(Collectors.joining(",")))
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
    public synchronized LinkedHashMap<String, ServiceLoader.Provider<? extends T>> getNamedProviders(Pattern... includes) {
        ServiceLoader<? extends T> loader = ServiceLoader.load(serviceType);
        List<Pattern> patterns = (includes != null && includes.length > 0) ? Arrays.asList(includes) : List.of(Pattern.compile(".*"));

        LinkedHashMap<String, ServiceLoader.Provider<? extends T>> providers;
        providers = new LinkedHashMap<>();

        loader.stream().forEach(provider -> {
            Class<? extends T> type = provider.type();
            if (!type.isAnnotationPresent(Service.class)) {
                throw new RuntimeException(
                    "Annotator services must be annotated with distinct selectors\n" +
                        "such as @Service(Annotator.class,selector=\"myimpl42\")"
                );
            }
            Service service = type.getAnnotation(Service.class);
            for (Pattern pattern : patterns) {
                if (pattern.matcher(service.selector()).matches()) {
                    providers.put(service.selector(), provider);
                    break;
                }
            }
        });

        return providers;
    }

    public List<String> getAllSelectors(Pattern... patterns) {
        return new ArrayList<>(getNamedProviders(patterns).keySet());
    }
}
