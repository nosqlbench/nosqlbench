/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.api.markdown.aggregator;

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.util.ast.Document;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.markdown.FlexParser;
import io.nosqlbench.api.markdown.types.FrontMatterInfo;
import io.nosqlbench.api.markdown.types.HasDiagnostics;
import io.nosqlbench.api.markdown.types.MarkdownInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

/**
 * TODO: Make this a value type
 */
public class ParsedMarkdown implements MarkdownInfo, HasDiagnostics {
    private final static Logger logger = LogManager.getLogger(MarkdownDocs.class);

    private final ParsedFrontMatter frontMatter;
    private final Content<?> content;

    public ParsedMarkdown(Content<?> content) {
        String rawMarkdown = content.asString();
        AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
        Document parsed = FlexParser.parser.parse(rawMarkdown);
        v.visit(parsed);
        Map<String, List<String>> data = v.getData();
        frontMatter = new ParsedFrontMatter(data);
        this.content = content;
        logger.debug("created " + this);
    }

    private ParsedMarkdown(ParsedFrontMatter frontMatter, Content<?> content) {
        this.frontMatter = frontMatter;
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
    public FrontMatterInfo getFrontmatter() {
        return frontMatter;
    }

    /**
     * Get a list of diagnostic warnings that might help users know of issues in their
     * markdown content before publication.
     * @param buffer A buffer object, for accumulating many lines of detail, if necessary.
     * @return The buffer, with possible additions
     */
    @Override
    public List<String> getDiagnostics(List<String> buffer) {
        List<String> diagnostics = frontMatter.getDiagnostics();
        if (diagnostics.size()==0) {
            return List.of();
        }
        String[] diags = diagnostics.stream().map(s -> " " + s).toArray(String[]::new);
        buffer.add("found " + diagnostics.size() + " diagnostics for " + getPath().toString());
        buffer.addAll(Arrays.asList(diags));
        return buffer;
    }

    /**
     * The buffer-less version of {@link #getDiagnostics(List)}
     * @return a list of diagnostics lines, zero if there are none
     */
    @Override
    public List<String> getDiagnostics() {
        return getDiagnostics(new ArrayList<>());
    }

    @Override
    public boolean hasAggregations() {
        return getFrontmatter().getAggregations().size()>0;
    }

    @Override
    public MarkdownInfo withTopics(List<String> assigning) {
        return new ParsedMarkdown(frontMatter.withTopics(assigning), this.content);
    }

    public MarkdownInfo withIncluded(List<String> included) {
        return new ParsedMarkdown(frontMatter.withIncluded(included), this.content);
    }

    @Override
    public String toString() {
        return "ParsedMarkdown/" +
            frontMatter.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedMarkdown that = (ParsedMarkdown) o;
        return Objects.equals(frontMatter, that.frontMatter) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frontMatter, content);
    }
}
