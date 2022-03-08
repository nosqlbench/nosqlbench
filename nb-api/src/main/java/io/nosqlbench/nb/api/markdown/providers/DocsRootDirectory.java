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

package io.nosqlbench.nb.api.markdown.providers;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.content.NBIOWalker;
import io.nosqlbench.nb.api.content.PathContent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class DocsRootDirectory implements RawMarkdownSource {

    @Override
    public List<Content<?>> getMarkdownInfo() {
        List<Content<?>> list = NBIO.local().name(getRootPathName()).list();
        NBIOWalker.CollectVisitor capture = new NBIOWalker.CollectVisitor(true, false);
        NBIOWalker.RegexFilter filter = new NBIOWalker.RegexFilter("\\.md",true);
        for (Content<?> content : list) {
            Path path = content.asPath();
            NBIOWalker.walkFullPath(path,capture,filter);
        }
        List<Content<?>> content = new ArrayList<>();
        for (Path path : capture.get()) {
            content.add(new PathContent(path));
        }
        return content;

    }

    protected abstract String getRootPathName();

}
