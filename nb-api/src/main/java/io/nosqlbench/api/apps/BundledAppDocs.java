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

package io.nosqlbench.api.apps;

import io.nosqlbench.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.api.spi.SimpleServiceLoader;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "apps")
public class BundledAppDocs implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        DocsBinder docs = new Docs();
        SimpleServiceLoader<BundledApp> loader = new SimpleServiceLoader<>(BundledApp.class, Maturity.Any);
        List<SimpleServiceLoader.Component<? extends BundledApp>> namedProviders = loader.getNamedProviders();
        for (SimpleServiceLoader.Component<? extends BundledApp> namedProvider : namedProviders) {
            BundledApp app = namedProvider.provider.get();
            DocsBinder bundledDocs = app.getBundledDocs();
            docs = docs.merge(bundledDocs);
        }
        return docs;
    }
}
