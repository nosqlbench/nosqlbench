/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class BundledExtensions {
    private final static Logger logger = LogManager.getLogger(BundledExtensions.class);
    private final NBComponent parent;

    public BundledExtensions(NBComponent parent) {
        this.parent = parent;
    }

    public static ScriptingExtensionPluginInfo<?>[] findAll() {
        return ServiceLoader.load(ScriptingExtensionPluginInfo.class).stream()
            .map(l -> l.get()).toArray(ScriptingExtensionPluginInfo[]::new);
    }

    public <T> Optional<T> load(String name, Class<T> type) {

        ServiceLoader<ScriptingExtensionPluginInfo> loader = ServiceLoader.load(ScriptingExtensionPluginInfo.class);
        return (Optional<T>) loader
            .stream()
            .filter(p -> {
                return Arrays.stream(p.type().getAnnotationsByType(Service.class)).toList().get(0).selector().equals(name);
            })
            .map(p -> p.get().getExtensionObject(logger, parent))
            .findAny()
            .filter(raw -> type.isAssignableFrom(raw.getClass()))
            .stream().findAny();
    }
}
