package io.nosqlbench.nb.api.markdown.aggregator;

import java.nio.file.Path;

public class MutableMarkdownInfo implements MarkdownInfo {
    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public FrontMatter getFrontmatter() {
        return null;
    }
}
