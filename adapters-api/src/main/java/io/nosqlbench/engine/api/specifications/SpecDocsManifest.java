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

package io.nosqlbench.engine.api.specifications;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "specifications")
public class SpecDocsManifest implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        Docs docs = new Docs().namespace("workload_definition");
        List<Content<?>> specfiles = NBIO.classpath().prefix("workload_definition/").extension(".md").list();
        for (Content<?> specfile : specfiles) {
            docs.addPath(specfile.asPath());
        }
        return docs;
    }
}
