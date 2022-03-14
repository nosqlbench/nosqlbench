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

package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;
import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeMarkdownInfo implements MarkdownInfo {
    private final List<MarkdownInfo> elements = new LinkedList<>();
    private boolean isSorted=false;

    @Override
    public Path getPath() {
        return elements.get(0).getPath();
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();
        if (!isSorted) {
            Collections.sort(elements);
            isSorted=true;
        }
        for (MarkdownInfo element : elements) {
            sb.append(element.getBody());
        }
        return sb.toString();
    }

    @Override
    public FrontMatterInfo getFrontmatter() {
        return elements.get(0).getFrontmatter();
    }

    @Override
    public boolean hasAggregations() {
        return false;
    }

    @Override
    public CompositeMarkdownInfo withTopics(List<String> assigning) {
        MarkdownInfo leader = elements.get(0);
        leader = leader.withTopics(assigning);
        elements.set(0,leader);
        return this;
    }

    public CompositeMarkdownInfo withIncluded(List<String> included) {
        MarkdownInfo leader = elements.get(0);
        leader = leader.withIncluded(included);
        elements.set(0,leader);
        return this;
    }

    public <T extends MarkdownInfo> CompositeMarkdownInfo add(T element) {
        elements.add(element);
        isSorted=false;
        return this;
    }

    @Override
    public String toString() {
        return "CompositeMarkdownInfo{" +
                "elements=" + elements +
                ", isSorted=" + isSorted +
                '}';
    }
}
