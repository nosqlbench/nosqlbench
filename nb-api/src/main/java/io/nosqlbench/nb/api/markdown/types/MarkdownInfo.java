package io.nosqlbench.nb.api.markdown.types;

import io.nosqlbench.nb.api.markdown.types.FrontMatterInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface MarkdownInfo {

    Path getPath();

    String getBody();

    FrontMatterInfo getFrontmatter();

    boolean hasAggregations();

    default boolean hasTopicGlobs() {
        return getTopicGlobs().size()>0;
    }
    default List<Pattern> getTopicGlobs() {
        return getFrontmatter().getTopics().stream()
            .filter(t -> t.startsWith("^") || t.endsWith("$") || t.contains(".*") || t.contains(".+"))
            .map(Pattern::compile)
            .collect(Collectors.toList());
    }

    default List<String> getTopics() {
        return getFrontmatter().getTopics().stream()
            .filter(t -> !t.startsWith("^") && !t.endsWith("$") && !t.contains(".*") && !t.contains(".+"))
            .collect(Collectors.toList());
    }

    default boolean hasAggregators() {
        return getFrontmatter().getAggregations().size()>0;
    }
    default List<Pattern> getAggregators() {
        return getFrontmatter().getAggregations();
    }

    MarkdownInfo withTopics(List<String> assigning);
}
