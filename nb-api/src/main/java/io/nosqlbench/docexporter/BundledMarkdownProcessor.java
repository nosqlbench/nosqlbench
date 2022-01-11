package io.nosqlbench.docexporter;

import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;

import java.util.function.Function;

public interface BundledMarkdownProcessor extends Function<MutableMarkdown, MutableMarkdown> {
    @Override
    MutableMarkdown apply(MutableMarkdown parsedMarkdown);
}
