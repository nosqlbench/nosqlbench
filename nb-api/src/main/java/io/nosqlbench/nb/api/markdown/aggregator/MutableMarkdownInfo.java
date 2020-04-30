package io.nosqlbench.nb.api.markdown.aggregator;

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.util.ast.Document;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.markdown.FlexParser;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class MutableMarkdownInfo implements MarkdownInfo {

    private final FrontMatter frontMatter;
    private final Content<?> content;

    public MutableMarkdownInfo(Content<?> content) {
        String rawMarkdown = content.asString();
        AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
        Document parsed = FlexParser.parser.parse(rawMarkdown);
        v.visit(parsed);
        Map<String, List<String>> data = v.getData();
        frontMatter = new MutableFrontMatter(data);
        this.content = content;
    }

    @Override
    public Path getPath() {
        return content.asPath();
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public FrontMatter getFrontmatter() {
        return frontMatter;
    }
}
