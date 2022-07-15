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

package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.spi.SimpleServiceLoader;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "adapter-docs")
public class BundledDriverAdapterDocs implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        Docs docs = new Docs().namespace("adapter-docs");
        SimpleServiceLoader<DriverAdapter> loader = new SimpleServiceLoader<>(DriverAdapter.class, Maturity.Any);
        List<SimpleServiceLoader.Component<? extends DriverAdapter>> namedProviders = loader.getNamedProviders();
        for (SimpleServiceLoader.Component<? extends DriverAdapter> namedProvider : namedProviders) {
            DriverAdapter driverAdapter = namedProvider.provider.get();
            DocsBinder bundledDocs = driverAdapter.getBundledDocs();
            docs.merge(bundledDocs);
        }
        return docs;
    }
}
