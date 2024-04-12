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
package io.nosqlbench.virtdata.core.bindings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Convenient singleton for accessing all loadable DataMapper Library instances.
 */
public class DataMapperLibraryFinder {

    private static final Logger logger =
            LogManager.getLogger(DataMapperLibrary.class);

    private static final Map<String, DataMapperLibrary> libraries = new ConcurrentHashMap<>();

    private DataMapperLibraryFinder() {
    }

    public synchronized static DataMapperLibrary get(String libraryName) {
        Optional<DataMapperLibrary> at = Optional.ofNullable(getLibraries().get(libraryName));
        return at.orElseThrow(
                () -> new RuntimeException("DataMapperLibrary '" + libraryName + "' not found.")
        );
    }

    private synchronized static Map<String, DataMapperLibrary> getLibraries() {
        if (libraries.size()==0) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            logger.debug("loading DataMapper Libraries");
            ServiceLoader<DataMapperLibrary> sl = ServiceLoader.load(DataMapperLibrary.class);
            Map<String,Integer> dups = new HashMap<>();
            for (DataMapperLibrary dataMapperLibrary : sl) {
                logger.debug(() -> "Found data mapper library:" +
                        dataMapperLibrary.getClass().getCanonicalName() + ":" +
                        dataMapperLibrary.getLibraryName());

                if (libraries.get(dataMapperLibrary.getLibraryName()) != null) {
                    String name = dataMapperLibrary.getLibraryName();
                    dups.put(name,dups.getOrDefault(name,0));
                }

                libraries.put(dataMapperLibrary.getLibraryName(),dataMapperLibrary);
            }
            if (dups.size() > 0) {
                logger.trace(() -> "Java runtime provided duplicates for " +
                        dups.entrySet().stream().map(e -> e.getKey()+":"+e.getValue()).collect(Collectors.joining(",")));
            }

        }
        logger.info(() -> "Loaded DataMapper Libraries:" + libraries.keySet());
        return libraries;
    }

    /**
     * Return list of libraries that have been found by this runtime,
     * in alphabetical order of their type names.
     * @return a list of DataMapperLibrary instances.
     */
    public synchronized static List<DataMapperLibrary> getAll() {
        List<DataMapperLibrary> libraries = new ArrayList<>(getLibraries().values());
        libraries.sort((o1, o2) -> o1.getLibraryName().compareTo(o2.getLibraryName()));
        return Collections.unmodifiableList(libraries);
    }
}
