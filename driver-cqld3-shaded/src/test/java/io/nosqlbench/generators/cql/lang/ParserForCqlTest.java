package io.nosqlbench.generators.cql.lang;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ParserForCqlTest {

    @Test
    public void parseAll() {
        List<Path> cql3_examples = getSubPaths("cql3_examples");

        for (Path examplePath : cql3_examples) {
            try {
                String example = Files.readString(examplePath, StandardCharsets.UTF_8);
                ParserForCql.parse(example);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static List<Path> getSubPaths(String resourcePath) {
        List<Path> subpaths = new ArrayList<>();

        try {
            Enumeration<URL> resources = ParserForCqlTest.class.getClassLoader().getResources(resourcePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("url=" + url.toExternalForm());
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
