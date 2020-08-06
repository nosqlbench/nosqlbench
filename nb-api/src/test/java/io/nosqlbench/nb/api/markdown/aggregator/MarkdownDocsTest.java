package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.content.PathContent;
import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;
import org.junit.Test;

import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

public class MarkdownDocsTest {

    @Test
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
                Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                        .filter(p -> !Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                        .forEach(subpaths::add);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subpaths;

    }


}
