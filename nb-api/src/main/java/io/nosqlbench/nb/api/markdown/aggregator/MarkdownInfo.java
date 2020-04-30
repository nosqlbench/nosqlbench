package io.nosqlbench.nb.api.markdown.aggregator;

import java.nio.file.Path;

public interface MarkdownInfo {

    Path getPath();
    String getBody();
    FrontMatter getFrontmatter();

}
