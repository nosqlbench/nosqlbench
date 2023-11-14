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

package io.nosqlbench.adapters.api.activityimpl.docs;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.nb.api.docsapi.Docs;
import io.nosqlbench.nb.api.docsapi.DocsBinder;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.spi.SimpleServiceLoader;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;

import java.util.List;

@Service(value = BundledMarkdownManifest.class, selector = "drivers")
public class BundledDriverAdapterDocs implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        NBComponent root = NBComponent.EMPTY_COMPONENT;
        DocsBinder docs = new Docs();
        SimpleServiceLoader<DriverAdapterLoader> loader = new SimpleServiceLoader<>(DriverAdapterLoader.class, Maturity.Any);
        List<SimpleServiceLoader.Component<? extends DriverAdapterLoader>> namedProviders = loader.getNamedProviders();
        for (SimpleServiceLoader.Component<? extends DriverAdapterLoader> namedProvider : namedProviders) {
            DriverAdapterLoader driverAdapterLoader = namedProvider.provider.get();
            DriverAdapter<Op, Object> driverAdapter = driverAdapterLoader.load(NBComponent.EMPTY_COMPONENT, NBLabels.forKV());
            DocsBinder bundledDocs = driverAdapter.getBundledDocs();
            docs = docs.merge(bundledDocs);
        }
        return docs;
    }
}
