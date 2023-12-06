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

package io.nosqlbench.nb.api.docsapi;

import java.nio.file.Path;
import java.util.*;

public class DocsNameSpaceImpl implements DocsNameSpace {

    private final Set<Path> paths = new HashSet<>();
    private String namespace;
    private boolean enabledByDefault = false;

    public DocsNameSpaceImpl() {}

    public static DocsNameSpaceImpl of(String descriptiveName, Path path) {
        return new DocsNameSpaceImpl().setNameSpace(descriptiveName).addPath(path);
    }

    private DocsNameSpaceImpl setNameSpace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DocsNameSpaceImpl(String name) {
        this.namespace = name;
    }

    public String getName() {
        return namespace;
    }

    @Override
    public List<Path> getPaths() {
        return new ArrayList<>(this.paths);
    }

    @Override
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    @Override
    public String toString() {
        return "DocPath{" +
                "namespace='" + namespace + '\'' +
                ",paths=" + paths +
                '}';
    }

    public DocsNameSpaceImpl addPath(Path path) {
        this.paths.add(path);
        return this;
    }

    public DocsNameSpaceImpl enabledByDefault() {
        this.enabledByDefault=true;
        return this;
    }

    @Override
    public Iterator<Path> iterator() {
        return this.paths.iterator();
    }

    public DocsNameSpaceImpl setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault=enabledByDefault;
        return this;
    }
}
