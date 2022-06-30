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

package io.nosqlbench.nb.annotations;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A service loader filter which works with {@link io.nosqlbench.nb.annotations.Service} to load a named service.
 * This version requires the caller to provide the service loader instance, since it is now caller sensitive.
 *
 * Use it like this:<pre>{@code
 *       ResultValueFilterType filterType =
 *           ServiceSelector.of("core", ServiceLoader.load(ResultValueFilterType.class)).get();
 * }</pre>
 *
 * @param <T> The service type
 */
public class ServiceSelector<T> implements Predicate<ServiceLoader.Provider<? extends T>> {
    private final String name;
    private final ServiceLoader<? extends T> loader;

    public ServiceSelector(String name, ServiceLoader<? extends T> loader) {
        this.name = name;
        this.loader = loader;
    }

    public static <T> ServiceSelector<T> of(String name, ServiceLoader<? extends T> loader) {
        return new ServiceSelector<>(name, loader);
    }

    public static <T> boolean matches(String name, ServiceLoader.Provider<? extends T> provider) {
        Service annotation = provider.type().getAnnotation(Service.class);
        if (annotation == null) {
            return false;
        }
        return (annotation.selector().equals(name));
    }

    @Override
    public boolean test(ServiceLoader.Provider<? extends T> provider) {
        return false;
    }

    public T getOne() {
        List<? extends T> services = getAll();
        if (services.size() == 0) {
            throw new RuntimeException("You requested exactly one instance of a service by name '" + name + "', but got " +
                (services.stream().map(s -> s.getClass().getSimpleName())).collect(Collectors.joining(",")) + " (" + services.stream().count() + ")");
        } else if (services.size()==1) {
            return services.get(0);
        }
        throw new RuntimeException("You requested exactly one instance of a service by name '" + name + "', but got " +
            (services.stream().map(s -> s.getClass().getSimpleName())).collect(Collectors.joining(",")) + " (" + services.stream().count() + ")");

    }

    public List<? extends T> getAll() {
        List<? extends T> services = loader
            .stream()
            .peek(l -> {
                    if (l.type().getAnnotation(Service.class) == null) {
                        throw new RuntimeException(
                            "Annotator services must be annotated with distinct selectors\n" +
                                "such as @Selector(\"myimpl42\")"
                        );
                    }
                }
            )
            .filter(l -> l.type().getAnnotation(Service.class) != null)
            .filter(l -> l.type().getAnnotation(Service.class).selector().equals(name))
            .map(ServiceLoader.Provider::get)
            .toList();
        return services;
    }

    public Optional<? extends T> get() {
        List<? extends T> services = getAll();
        if (services.size() == 1) {
            return Optional.of(services.get(0));
        } else {
            return Optional.empty();
        }
    }
}
