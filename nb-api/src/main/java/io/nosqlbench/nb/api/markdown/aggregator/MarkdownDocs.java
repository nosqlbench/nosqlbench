package io.nosqlbench.nb.api.markdown.aggregator;

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.markdown.FlexParser;
import io.nosqlbench.nb.api.markdown.providers.MarkdownProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarkdownDocs {

    private static FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder()
        .extensions(List.of(YamlFrontMatterExtension.create()))
        .build();

    public static MarkdownInfo find(DocScope... scopes) {
        return null;
    }

    public static List<MarkdownInfo> find(String name, DocScope... scopes) {
        List<MarkdownInfo> aggregated = new ArrayList<>();

        List<Content<?>> markdownContent = MarkdownProviders.getAllMarkdown();
        MarkdownInfo info = new MutableMarkdownInfo();

        // Find all topics and aggregators
        List<String> aggregators = new ArrayList<>();
        List<String> topics = new ArrayList<>();

        for (Content<?> content : markdownContent) {
            String markdown = content.asString();
            String convert = converter.convert(markdown);

            AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
            Document parsed = FlexParser.parser.parse(markdown);

            v.visit(parsed);
            Map<String, List<String>> data = v.getData();
            System.out.print("frontmatter for " + content.asPath());
            for (Map.Entry<String, List<String>> e : data.entrySet()) {
                System.out.println("k : " + e.getKey());
                System.out.println(" v:" + String.join(",",e.getValue()));
            }
        }

        return aggregated;


    }
}
