package io.nosqlbench.nb.api.markdown.descriptor;

import java.nio.file.Path;

public interface MarkdownInfo {
    Path getPath();
    CharSequence getBody();
    FrontMatter getFrontmatter();

}
