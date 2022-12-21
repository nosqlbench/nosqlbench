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
package io.nosqlbench.engine.core.lifecycle.process;

import io.nosqlbench.engine.api.activityapi.core.Shutdownable;

import java.util.LinkedList;

/**
 * A simple callback handler for shutting down things gracefully.
 */
public class ShutdownManager {
    private ShutdownManager() {
    }

    private static final ShutdownManager instance = new ShutdownManager();

    private final LinkedList<Shutdownable> managedInstances = new LinkedList<>();

    public static void register(Shutdownable managedInstance) {
        instance.managedInstances.add(managedInstance);
    }

    public static void shutdown() {
        instance.managedInstances.forEach(Shutdownable::shutdown);
    }
}
