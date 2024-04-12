/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.core.threadstate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This provides common thread local instancing for sharing a thread local map across classes.
 * This is being described as a <em>Thread Local State Cache</em>.
 */
public class SharedState {

    public enum Scope {
        process,
        thread
    }

    // A thread-local map of objects by name
    public static ThreadLocal<HashMap<String,Object>> tl_ObjectMap = ThreadLocal.withInitial(HashMap::new);

    // A thread-local stack of objects by name
    public static ThreadLocal<Deque<Object>> tl_ObjectStack = ThreadLocal.withInitial(ArrayDeque::new);

    // A global map of objects for constant pool, etc.
    public static ConcurrentHashMap<String,Object> gl_ObjectMap =
            new ConcurrentHashMap<>();

    public static <T> T put(Scope scope, String name, T value) {
        return switch(scope) {
            case process -> (T) gl_ObjectMap.put(name, value);
            case thread -> (T) tl_ObjectMap.get().put(name, value);
        };
    }

    public static <T> T get(Scope scope, String name) {
        return switch(scope) {
            case process -> (T) gl_ObjectMap.get(name);
            case thread -> (T) tl_ObjectMap.get().get(name);
        };
    }

}
