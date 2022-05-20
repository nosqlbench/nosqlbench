package io.nosqlbench.nb.api;

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


public enum OnError {
    Warn,
    Throw;

    public static OnError valueOfName(String name) {
        for (OnError value : OnError.values()) {
            if (value.toString().toLowerCase().equals(name.toLowerCase())) {
                return value;
            }
        }
        throw new RuntimeException("No matching OnError enum value for '" + name + "'");
    }
}
