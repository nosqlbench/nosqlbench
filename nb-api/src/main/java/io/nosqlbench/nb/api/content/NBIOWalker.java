package io.nosqlbench.nb.api.content;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

public class NBIOWalker {
    private final static Logger logger = LogManager.getLogger(NBIOWalker.class);

    public static void walk(Path p, PathVisitor v) {
        walkShortPath(p, v, NBIOWalker.WALK_ALL);
    }

    public static List<Path> findAll(Path p) {
        Collect fileCollector = new Collect(true, false);
        walk(p, fileCollector);
        return fileCollector.get();

    }

    /**
     * This walks the directory structure starting at the path specified. The path visitor is invoked for every
     * directory, and every non-directory which matches the filter.
     * This form uses only the filename component in Paths to be matched by the filter, and the short name is also
     * what is returned by the filter.
     *
     * @param p The path to search
     * @param v The visitor to accumulate or operate on matched paths and all directories
     * @param filter The Path filter to determine whether a path is included
     */
    public static void walkShortPath(Path p, PathVisitor v, DirectoryStream.Filter<Path> filter) {
        walk(null, p, v, filter, false);
    }

    /**
     * This walks the directory structure starting at the path specified. The path visitor is invoked for every
     * directory, and every non-directory which matches the filter.
     * This form uses only the full path from the initial search path root in all Paths to be matched by
     * the filter, and this form of a Path component is also returned in all Paths seen by the visitor.
     *
     * @param p The path to search
     * @param v The visitor to accumulate or operate on matched paths and all directories
     * @param filter The Path filter to determine whether a path is included
     */
    public static void walkFullPath(Path p, PathVisitor v, DirectoryStream.Filter<Path> filter) {
        walk(null, p, v, filter, true);
    }

    public static void walk(Path root, Path p, PathVisitor v, DirectoryStream.Filter<Path> filter, boolean fullpath) {

        try {
            FileSystemProvider provider = p.getFileSystem().provider();
            DirectoryStream<Path> paths = provider.newDirectoryStream(p, (Path r) -> true);
            List<Path> pathlist = new ArrayList<>();

            for (Path path : paths) {
                pathlist.add(path);
            }

            for (Path path : pathlist) {
                if (fullpath && root != null) {
                    path = root.resolve(path);
                }

                if (path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class).isDirectory()) {
                    v.preVisitDir(path);
                    walk(root, path, v, filter, fullpath);
                    v.postVisitDir(path);
                } else if (filter.accept(path)) {
                    v.preVisitFile(path);
                    v.visit(path);
                    v.postVisitFile(path);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface PathVisitor {
        void visit(Path p);

        default void preVisitFile(Path path) {
        }

        default void postVisitFile(Path path) {
        }

        default void preVisitDir(Path path) {
        }

        default void postVisitDir(Path path) {
        }
    }

    public static DirectoryStream.Filter<Path> WALK_ALL = entry -> true;

    public static class Collect implements PathVisitor {
        private final List<Path> listing = new ArrayList<>();
        private final boolean collectFiles;
        private final boolean collectDirectories;

        public Collect(boolean collectFiles, boolean collectDirectories) {

            this.collectFiles = collectFiles;
            this.collectDirectories = collectDirectories;
        }

        public List<Path> get() {
            return listing;
        }

        @Override
        public void visit(Path p) {
        }

        @Override
        public void preVisitFile(Path path) {
            if (this.collectFiles) {
                listing.add(path);
            }
        }

        @Override
        public void preVisitDir(Path path) {
            if (this.collectDirectories) {
                listing.add(path);
            }
        }
    }

}
