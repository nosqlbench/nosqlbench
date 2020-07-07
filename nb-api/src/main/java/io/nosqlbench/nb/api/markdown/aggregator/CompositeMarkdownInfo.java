package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;
import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class CompositeMarkdownInfo implements MarkdownInfo {
    private List<MarkdownInfo> elements = new LinkedList<>();

    @Override
    public Path getPath() {
        return elements.get(0).getPath();
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();
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
    public MarkdownInfo withTopics(List<String> assigning) {
        MarkdownInfo leader = elements.get(0);
        leader = leader.withTopics(assigning);
        elements.set(0,leader);
        return this;
    }

    public <T extends MarkdownInfo> CompositeMarkdownInfo add(T element) {
        elements.add(element);
        return this;
    }
}
