package io.nosqlbench.nb.api.markdown.providers;

import io.nosqlbench.nb.api.content.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The internal
 */
public class MarkdownProviders {

    public static List<Content<?>> getAllMarkdown() {
        ServiceLoader<MarkdownProvider> loader = ServiceLoader.load(MarkdownProvider.class);
        List<Content<?>> names = new ArrayList<>();
        loader.iterator().forEachRemaining(d -> names.addAll(d.getMarkdownInfo()));
        return names;
    }
}
