package io.nosqlbench.virtdata.userlibs.docinfo;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;

@Service(value = DocsysDynamicManifest.class, selector = "virtdata-docs")
public class VirtdataMarkdownManifest implements DocsysDynamicManifest {

    public DocsBinder getDocs() {
        return new Docs().namespace("virtdata-docs").addFirstFoundPath(
                "virtdata-userlibs/src/main/resources/docs-for-virtdata/",
                "docs-for-virtdata/")
                .setEnabledByDefault(true)
                .asDocsBinder();
    }

}
