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

package io.nosqlbench.docapi;

import io.nosqlbench.api.docsapi.BundledMarkdownManifest;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Service;

@Service(value = BundledMarkdownManifest.class, selector = "bundled-markdown-test-docs")
public class BundledMarkdownTestManifest implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs()
            .namespace("bundled-markdown-test-docs")
            .addFirstFoundContentPath(
                "nb-api/src/test/resources/testsite1/",
                "testsite1"
            )
            .asDocsBinder();
    }
}
