/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.markdown.aggregator;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.WhiteSpace;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.BlankLine;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.nb.api.markdown.FlexParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MutableMarkdown  {
    private final static Logger logger = LogManager.getLogger(MarkdownDocs.class);

    private MutableFrontMatter frontMatter;
    private final String rawMarkdown;
    private final Path path;
    private Heading firstHeading;

    public MutableMarkdown(Path path) {
        try {
            this.path = path;
            this.rawMarkdown = Files.readString(path);
            parseStructure(rawMarkdown);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public MutableMarkdown(String rawMarkdown) {
        this.path = null;
        this.rawMarkdown = rawMarkdown;
        parseStructure(rawMarkdown);
    }

    private void parseStructure(String rawMarkdown) {
        AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
        Document parsed = FlexParser.parser.parse(rawMarkdown);
        v.visit(parsed);
        Map<String, List<String>> data = v.getData();
        this.frontMatter = new MutableFrontMatter(data);

        if (frontMatter.getTitle()==null || frontMatter.getTitle().isEmpty()) {
            Node node = parsed.getFirstChild();
            while (node!=null) {
                if (node instanceof Heading) {
                    this.frontMatter.setTitle(((Heading) node).getText().toString());
                    break;
                } else if (node instanceof BlankLine) {
                } else if (node instanceof WhiteSpace) {
                } else if (node instanceof YamlFrontMatterBlock) {
                } else {
                    if(this.path != null)
                        throw new RuntimeException("The markdown file at '" + this.path.toString() + "' must have an initial heading as a title, before any other element, but found:" + node.getClass().getSimpleName());
                    else
                        throw new RuntimeException("The markdown string provided must have an initial heading as a title, before any other element, but found: "+ node.getClass().getSimpleName());
                }
                node=node.getNext();
            }
        }
        if (frontMatter.getTitle()==null || frontMatter.getTitle().isEmpty()) {
            if(this.path != null)
                throw new RuntimeException("The markdown file at '" + this.path.toString() + "' has no heading to use as a title.");
            else
                throw new RuntimeException("The markdown string provided has no heading to use as a title.");
        }
    }

    public Path getPath() {
        return path;
    }

    public String getBody() {
        for (String boundary : List.of("---\n", "+++\n")) {
            if (rawMarkdown.startsWith(boundary)) {
                int end = rawMarkdown.indexOf(boundary, 3);
                if (end>=0) {
                    return rawMarkdown.substring(end+4);
                } else {
                    if(path != null)
                        throw new RuntimeException("Unable to find matching boundaries in " + path.toString() + ": " + boundary);
                    else
                        throw new RuntimeException("Unable to find matching boundaries in provided markdown: " + boundary);
                }
            }
        }
        return rawMarkdown;
    }

    public MutableFrontMatter getFrontmatter() {
        return frontMatter;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "/" +
            frontMatter.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableMarkdown that = (MutableMarkdown) o;
        return Objects.equals(frontMatter, that.frontMatter) &&
                Objects.equals(rawMarkdown, that.rawMarkdown);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frontMatter, rawMarkdown);
    }

    public String getComposedMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append(frontMatter.asYaml());
        sb.append("---\n");

        sb.append(getBody());
        return sb.toString();
    }
}
