package io.nosqlbench.nb.api.markdown.types;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface MarkdownInfo extends Comparable<MarkdownInfo> {

    Path getPath();

    String getBody();

    FrontMatterInfo getFrontmatter();

    boolean hasAggregations();

    default boolean hasTopicGlobs() {
        return getTopicGlobs().size() > 0;
    }

    default List<Pattern> getTopicGlobs() {
        List<Pattern> pattern = getFrontmatter().getTopics().stream()
                .filter(t -> t.startsWith("^") || t.endsWith("$") || t.contains(".*") || t.contains(".+"))
                .map(Pattern::compile)
                .collect(Collectors.toList());
        return pattern;
    }

    default List<String> getTopics() {
        return getFrontmatter().getTopics().stream()
                .filter(t -> !t.startsWith("^") && !t.endsWith("$") && !t.contains(".*") && !t.contains(".+"))
                .collect(Collectors.toList());
    }

    default List<String> getIncluded() {
        return getFrontmatter().getIncluded();
    }


    default boolean hasAggregators() {
        return getFrontmatter().getAggregations().size() > 0;
    }

    default List<Pattern> getAggregators() {
        return getFrontmatter().getAggregations();
    }

    MarkdownInfo withTopics(List<String> assigning);

    default int compareTo(@NotNull MarkdownInfo o) {
        int diff = getFrontmatter().getWeight() - o.getFrontmatter().getWeight();
        if (diff != 0) return diff;
        diff = getFrontmatter().getTitle().compareTo(o.getFrontmatter().getTitle());
        if (diff!=0) return diff;
        diff = getBody().compareTo(o.getBody());
        return diff;
    }

    default boolean matchesTopicPattern(Pattern pattern) {
        return getTopics().stream().anyMatch(t -> pattern.matcher(t).matches());
    }

    MarkdownInfo withIncluded(List<String> included);



}
