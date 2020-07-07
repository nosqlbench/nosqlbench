package io.nosqlbench.nb.api.markdown.providers;

import io.nosqlbench.nb.api.content.Content;

import java.util.List;

/**
 * A MarkdownProvider simply provides all the markdown content it is aware of.
 */
public interface RawMarkdownSource {
    List<Content<?>> getMarkdownInfo();
}
