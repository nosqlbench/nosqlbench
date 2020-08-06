package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;
import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeMarkdownInfo implements MarkdownInfo {
    private List<MarkdownInfo> elements = new LinkedList<>();
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
