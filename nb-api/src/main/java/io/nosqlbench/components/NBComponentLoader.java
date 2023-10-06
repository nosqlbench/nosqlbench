package io.nosqlbench.components;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.api.spi.SimpleServiceLoader;
import io.nosqlbench.nb.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

public class NBComponentLoader {
    public static <C extends NBComponent> C load(NBComponent parent, String selector, Class<C> clazz) {
        ServiceLoader<C> loader = ServiceLoader.load(clazz);
        List<ServiceLoader.Provider<C>> providers = loader.stream().filter(p -> {
            Service service = Arrays.stream(p.type().getAnnotationsByType(Service.class)).findFirst().orElseThrow();
            return service.selector().equals(selector);
        }).toList();
        if (providers.size()!=1) {
            throw new RuntimeException("Loaded " + providers.size() + " providers for selector '" + selector + "', expected 1.");
        }
        ServiceLoader.Provider<C> cp = providers.get(0);
        try {
            Constructor<? extends C> ctor = cp.type().getConstructor(NBComponent.class);
            return ctor.newInstance(parent);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
