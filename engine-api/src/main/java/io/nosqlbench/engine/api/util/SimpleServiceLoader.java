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

package io.nosqlbench.engine.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SimpleServiceLoader<T extends Named> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceLoader.class);
    private Class<? extends T> serviceType;
    private static SimpleServiceLoader instance;

    private final Map<String, T> types = new ConcurrentHashMap<>();

    public SimpleServiceLoader(Class<? extends T> serviceType) {
        this.serviceType = serviceType;
    }

    public Optional<T> get(String implName) {
        return Optional.ofNullable(getTypes().get(implName));
    }

    public T getOrThrow(String implName) {
        Optional<T> at = Optional.ofNullable(getTypes().get(implName));
        return at.orElseThrow(
                () -> new RuntimeException(serviceType.getSimpleName() + " '" + implName + "' not found. Available types:" +
                        this.getTypes().keySet().stream().collect(Collectors.joining(",")))
        );
    }

    private synchronized Map<String, T> getTypes() {
        if (types.size()==0) {
            ClassLoader cl = getClass().getClassLoader();
            logger.debug("loading service types for " + serviceType.getSimpleName());
            ServiceLoader<? extends T> sl = ServiceLoader.load(serviceType);
            try {
                for (T inputType : sl) {
                    if (types.get(inputType.getName()) != null) {
                        throw new RuntimeException("ActivityType '" + inputType.getName()
                                + "' is already defined.");
                    }
                    types.put(inputType.getName(),inputType);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        logger.info("Loaded Types:" + types.keySet());
        return types;
    }

    public List<T> getAll() {
        List<T> types = new ArrayList<>(getTypes().values());
        types.sort(Comparator.comparing(Named::getName));
        return Collections.unmodifiableList(types);
    }

}
