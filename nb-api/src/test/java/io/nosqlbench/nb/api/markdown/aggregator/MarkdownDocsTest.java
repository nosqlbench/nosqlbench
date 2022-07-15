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

package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.api.content.PathContent;
import io.nosqlbench.api.markdown.aggregator.MarkdownDocs;
import io.nosqlbench.api.markdown.aggregator.ParsedMarkdown;
import io.nosqlbench.api.markdown.types.MarkdownInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownDocsTest {

    @Test
    @Disabled
    public void testLoadMarkdown() {
        List<MarkdownInfo> processed = MarkdownDocs.findAll();
        List<MarkdownInfo> expected = fromRaw("docs-for-testing-logical");

        Map<Path, MarkdownInfo> processedPaths = processed.stream().collect(Collectors.toMap(MarkdownInfo::getPath, v -> v));
        Map<Path, MarkdownInfo> expectedPaths = expected.stream().collect(Collectors.toMap(MarkdownInfo::getPath, v -> v));

        for (Path path : expectedPaths.keySet()) {
            System.out.println("expected path:" + path.toString());
        }

        Set<Path> missingPaths = new HashSet<>();
        for (Path path : expectedPaths.keySet()) {
            if (!processedPaths.containsKey(path)) {
                missingPaths.add(path);
            }
        }

        Set<Path> extraPaths = new HashSet<>();
        for (Path path : processedPaths.keySet()) {
            if (!expectedPaths.containsKey(path)) {
                extraPaths.add(path);
            }
        }

        for (MarkdownInfo markdownInfo : processed) {
            Path path = markdownInfo.getPath();
        }

        assertThat(missingPaths).isEmpty();
        assertThat(extraPaths).isEmpty();

    }

    private List<MarkdownInfo> fromRaw(String parentPath) {
        List<MarkdownInfo> fromraw = new ArrayList<>();
        List<Path> postpaths = getSubPaths("docs-for-testing-logical");
        for (Path postpath : postpaths) {
            PathContent content = new PathContent(postpath);
            ParsedMarkdown parsedMarkdown = new ParsedMarkdown(content);
            fromraw.add(parsedMarkdown);
        }
        Collections.sort(fromraw);
        return fromraw;
    }

    private static List<Path> getSubPaths(String resourcePath) {
        List<Path> subpaths = new ArrayList<>();

        try {
            Enumeration<URL> resources =
                    MarkdownDocsTest.class.getClassLoader().getResources(resourcePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("url="+url.toExternalForm());
                Path path = Paths.get(url.toURI());
                try (Stream<Path> fileStream = Files.walk(path, FileVisitOption.FOLLOW_LINKS)) {
                    fileStream.filter(p -> !Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                            .forEach(subpaths::add);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subpaths;
    }
}
