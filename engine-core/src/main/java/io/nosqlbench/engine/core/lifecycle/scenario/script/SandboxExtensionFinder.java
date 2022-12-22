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

package io.nosqlbench.engine.core.lifecycle.scenario.script;

import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class SandboxExtensionFinder {

    private final static List<ScriptingPluginInfo<?>> extensionDescriptors = new ArrayList<>();

    public static List<ScriptingPluginInfo<?>> findAll() {
        if (extensionDescriptors.isEmpty()) {
            synchronized (SandboxExtensionFinder.class) {
                if (extensionDescriptors.isEmpty()) {
                    ServiceLoader<ScriptingPluginInfo> loader =
                            ServiceLoader.load(ScriptingPluginInfo.class);
                    loader.iterator().forEachRemaining(extensionDescriptors::add);
                }
            }
        }

        return Collections.unmodifiableList(extensionDescriptors);

    }
}
