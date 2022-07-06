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

package io.nosqlbench.engine.clients.grafana;

public class ApiToken {
    private String name;
    private final String hashed;
    private String key;
    private final int id;

    public ApiToken(String name, String key) {
        this(1, name, key, null);
    }

    public ApiToken(int id, String name, String key) {
        this(id, name, key, null);
    }
    public ApiToken(int id, String name, String key, String hashed) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.hashed = hashed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHashed() {
        return hashed;
    }

    @Override
    public String toString() {
        return "ApiToken{" +
            "name='" + name + '\'' +
            ", hashed='" + hashed + '\'' +
            ", key='" + key + '\'' +
            '}';
    }

    public int getId() {
        return this.id;
    }
}
