package io.nosqlbench.nb.api.markdown.providers;

import io.nosqlbench.nb.api.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The internal
 */
public class RawMarkdownSources {

    public static List<Content<?>> getAllMarkdown() {
        ServiceLoader<RawMarkdownSource> loader = ServiceLoader.load(RawMarkdownSource.class);
        List<Content<?>> content = new ArrayList<>();
        loader.iterator().forEachRemaining(d -> content.addAll(d.getMarkdownInfo()));
        return content;
    }
}
