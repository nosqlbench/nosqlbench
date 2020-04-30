package io.nosqlbench.nb.api.markdown.aggregator;

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.ast.Document;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.markdown.FlexParser;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSources;

import java.util.*;
import java.util.stream.Collectors;

public class MarkdownDocs {

    public static List<MarkdownInfo> find(DocScope... scopes) {
        return find(".*", scopes);
    }

    public static List<MarkdownInfo> findAll() {
        return find(DocScope.ANY);
    }

    public static List<MarkdownInfo> find(String name, DocScope... scopes) {
        List<MarkdownInfo> aggregated = new ArrayList<>();

        List<Content<?>> markdownContent = RawMarkdownSources.getAllMarkdown();

        // Find all topics and aggregators
        List<String> aggregators = new ArrayList<>();

        List<MutableMarkdownInfo> markdownInfos = markdownContent
            .stream()
            .map(MutableMarkdownInfo::new)
            .collect(Collectors.toList());

        List<Set<String>> collect =
            markdownInfos.stream().map(m -> m.getFrontmatter().getTopics()).collect(Collectors.toList());

        // Assign glob topics

        // Assign content aggregates
        System.out.println("topics: " + collect);

        aggregated.addAll(markdownInfos);
        return aggregated;


    }

}
