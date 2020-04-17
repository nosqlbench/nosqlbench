package io.nosqlbench.nb.api.markdown.descriptor;

import java.util.List;

/**
 * A MarkdownProvider simply provides all the markdown content it is aware of.
 */
public interface MarkdownProvider {
    List<MarkdownInfo> getMarkdownInfo();
}
