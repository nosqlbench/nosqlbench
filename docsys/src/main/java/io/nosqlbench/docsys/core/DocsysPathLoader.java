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

package io.nosqlbench.docsys.core;

import io.nosqlbench.docapi.*;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;
import io.nosqlbench.docsys.api.DocsysStaticManifest;

import java.util.ServiceLoader;

/**
 * The standard way to load and use all of the {@link DocsNameSpaceImpl}
 * instances which are present in the runtime via SPI.
 *
 * This implementation ensures that names space collisions are known.
 */
public class DocsysPathLoader {

    public static DocsBinder loadStaticPaths() {
        ServiceLoader<DocsysStaticManifest> loader = ServiceLoader.load(DocsysStaticManifest.class);
        Docs docs = new Docs();
        for (DocsysStaticManifest docPathInfos : loader) {
            docs.merge(docPathInfos.getDocs());
        }
        return docs;
    }

    public static DocsBinder loadDynamicPaths() {
        ServiceLoader<DocsysDynamicManifest> loader = ServiceLoader.load(DocsysDynamicManifest.class);
        Docs docs = new Docs();
        for (DocsysDynamicManifest docPathInfos : loader) {
            docs.merge(docPathInfos.getDocs());
        }
        return docs;
    }


}
