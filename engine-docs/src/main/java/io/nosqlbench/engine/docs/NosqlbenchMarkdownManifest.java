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

package io.nosqlbench.engine.docs;

import io.nosqlbench.docapi.Docs;
import io.nosqlbench.docapi.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DocsysDynamicManifest.class, selector = "docs-for-eb")
public class NosqlbenchMarkdownManifest implements DocsysDynamicManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docs-for-eb")
            .addFirstFoundPath(
                "engine-docs/src/main/resources/docs-for-nb/",
                "docs-for-nb/"
            ).setEnabledByDefault(true)
            .asDocsBinder();
    }
}
