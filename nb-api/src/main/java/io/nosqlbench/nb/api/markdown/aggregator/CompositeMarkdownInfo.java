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
        // calculate included topics
        return null;
    }

    @Override
    public boolean hasAggregations() {
        // was true, but now it is false after compositing
        return false;
    }

    public <T extends MarkdownInfo> CompositeMarkdownInfo add(T element) {
        elements.add(element);
        return this;
    }
}
