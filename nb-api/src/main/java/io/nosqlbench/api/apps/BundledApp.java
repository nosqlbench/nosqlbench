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

package io.nosqlbench.api.apps;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.nb.annotations.Service;

import java.util.Optional;
import java.util.function.ToIntFunction;

public interface BundledApp extends ToIntFunction<String[]> {

    int applyAsInt(String[] value);

    default String getBundledAppName() {
        return this.getClass().getAnnotation(Service.class).selector();
    }

    default DocsBinder getBundledDocs() {
        Docs docs = new Docs().namespace("apps");

        String dev_docspath = "app-" + this.getBundledAppName() + "/src/main/resources/docs/" + this.getBundledAppName();
        String cp_docspath = "docs/" + this.getBundledAppName();
        Optional<Content<?>> bundled_docs = NBIO.local().name(dev_docspath, cp_docspath).first();
        bundled_docs.map(Content::asPath).ifPresent(docs::addContentsOf);

        Optional<Content<?>> maindoc = NBIO.local().name("/src/main/resources/" + this.getBundledAppName() + ".md", this.getBundledAppName() + ".md").first();

        maindoc.map(Content::asPath).ifPresent(docs::addPath);

        return docs.asDocsBinder();
    }

}
