package io.nosqlbench.nb.api.content;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NBIORelativizer {

    public static List<Path> relativizePaths(Path base, List<Path> contained) {
        List<Path> relativized = new ArrayList<>();
        for (Path path : contained) {
            Path relative = base.relativize(path);
            relativized.add(relative);
        }
        return relativized;
    }

    public static List<Path> relativizeContent(Path base, List<Content<?>> contained) {
        return relativizePaths(
            base,
            contained.stream().map(Content::asPath).collect(Collectors.toList()));
    }

}
