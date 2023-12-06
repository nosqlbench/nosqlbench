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

package io.nosqlbench.adapters.api.specifications;

import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.nb.api.docsapi.Docs;
import io.nosqlbench.nb.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "specifications")
public class SpecDocsManifest implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        Docs docs = new Docs().namespace("workload_definition");
        List<Content<?>> specfiles = NBIO.classpath().searchPrefixes("workload_definition/").extensionSet(".md").list();
        for (Content<?> specfile : specfiles) {
            docs.addPath(specfile.asPath());
        }
        return docs;
    }
}
