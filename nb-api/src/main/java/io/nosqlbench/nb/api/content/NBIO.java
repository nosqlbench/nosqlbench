package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.content.fluent.NBPathsAPI;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NBIO is a helper utility packaged as a search builder and fluent API.
 * It uses value semantics internally, so it is safe to re-use as a
 * stateful configuration builder for finding files in various ways.
 * <p>
 * Since this is meant to ease development around a usually over-complicated
 * surface area in the JVM (Files, Paths, URIs, accessing data, knowing where it comes
 * from, searching for it, etc), more emphasis was put on ease of use and
 * clarity than efficiency. This set of classes is not expected to be used
 * much in NoSqlBench after initialization.
 */
public class NBIO implements NBPathsAPI.Facets {

    private URIResolver resolver;
    private MatchType matchas = MatchType.exact;

    private List<String> names = new ArrayList<>();
    private List<String> extensions = new ArrayList<>();
    private List<String> searchPaths = new ArrayList<>();


    private enum MatchType {
        exact,
        suffix,
        pattern
    }

    private NBIO() {
    }

    private NBIO(URIResolver resolver,
                 MatchType matchas,
                 List<String> searchPaths,
                 List<String> names,
                 List<String> extensions) {
        this.resolver = resolver;
        this.matchas = matchas;
        this.searchPaths = searchPaths;
        this.names = names;
        this.extensions = extensions;
    }

    @Override
    public NBPathsAPI.ForContentSource localContent() {
        this.resolver = URIResolvers.inFS().inCP();
        return this;
    }

    @Override
    public NBPathsAPI.ForContentSource remoteContent() {
        this.resolver = URIResolvers.inURLs();
        return this;
    }

    @Override
    public NBPathsAPI.ForContentSource internalContent() {
        this.resolver = URIResolvers.inClasspath();
        return this;
    }

    @Override
    public NBPathsAPI.ForContentSource fileContent() {
        this.resolver = URIResolvers.inFS();
        return this;
    }

    @Override
    public NBPathsAPI.ForContentSource allContent() {
        this.resolver = URIResolvers.inFS().inCP().inURLs();
        return this;
    }

    @Override
    public NBPathsAPI.WantsContentName exact() {
        return new NBIO(resolver, MatchType.exact, searchPaths, names, extensions);
    }

    @Override
    public NBPathsAPI.WantsContentName matchtail() {
        return new NBIO(resolver, MatchType.suffix, searchPaths, names, extensions);
    }

    @Override
    public NBPathsAPI.WantsContentName regex() {
        return new NBIO(resolver, MatchType.pattern, searchPaths, names, extensions);
    }

    @Override
    public NBPathsAPI.ForPrefix prefix(String... searchPaths) {
        ArrayList<String> addingPaths = new ArrayList<>(this.searchPaths);
        addingPaths.addAll(Arrays.asList(searchPaths));
        return new NBIO(resolver, matchas, addingPaths, names, extensions);
    }

    @Override
    public NBPathsAPI.ForName name(String... searchNames) {
        ArrayList<String> addingNames = new ArrayList<>(this.names);
        addingNames.addAll(Arrays.asList(searchNames));
        return new NBIO(resolver, matchas, searchPaths, addingNames, extensions);
    }

    @Override
    public NBPathsAPI.ForExtension extension(String... extensions) {
        ArrayList<String> addingExtensions = new ArrayList<>(this.extensions);
        for (String addingExtension : extensions) {
            addingExtensions.add(addingExtension.startsWith(".") ? addingExtension : "." + addingExtension);
        }
        return new NBIO(resolver, matchas, searchPaths, names, addingExtensions);
    }

    /**
     * Search for named resources everywhere: URLs, filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.ForContentSource all() {
        return new NBIO().allContent();
    }

    /**
     * Search for named resources in the classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.ForContentSource classpath() {
        return new NBIO().internalContent();
    }

    /**
     * Search for named resources on the filesystem
     *
     * @return a builder
     */
    public static NBPathsAPI.ForContentSource fs() {
        return new NBIO().fileContent();
    }

    /**
     * Search for named resources locally: filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.ForContentSource local() {
        return new NBIO().localContent();
    }

    /**
     * Search for named resources only in URLs
     *
     * @return a builder
     */
    public static NBPathsAPI.ForContentSource remote() {
        return new NBIO().remoteContent();
    }


    @Override
    public Optional<Content<?>> first() {
        Content<?> found = null;
        LinkedHashSet<String> specificPathsToSearch = expandSearches();
        for (String candidatePath : specificPathsToSearch) {
            Content<?> content = resolver.resolve(candidatePath);
            if (content != null) {
                return Optional.of(content);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Optional<Content<?>>> resolveEach() {
        List<Optional<Content<?>>> resolved = new ArrayList<>();
        for (String name : names) {
            LinkedHashSet<String> slotSearchPaths = expandSearches(name);
            Content<?> content = null;
            for (String slotSearchPath : slotSearchPaths) {
                content = resolver.resolve(slotSearchPath);
                if (content != null) {
                    break;
                }
            }
            resolved.add(Optional.ofNullable(content));
        }

        return resolved;
    }


    // for testing
    public LinkedHashSet<String> expandSearches() {
        LinkedHashSet<String> searchSet = new LinkedHashSet<>(extensions.size() * names.size() * searchPaths.size());
        for (String name : names) {
            searchSet.addAll(expandSearches(name));
        }
        return searchSet;
    }

    // for testing
    public LinkedHashSet<String> expandSearches(String name) {

        LinkedHashSet<String> searchSet = new LinkedHashSet<>();

        List<String> searchPathsToTry = new ArrayList<>();
        searchPathsToTry.add("");
        searchPathsToTry.addAll(searchPaths);

        List<String> extensionsToTry = new ArrayList<>();
        extensionsToTry.add("");
        extensionsToTry.addAll(extensions);

        for (String searchPath : searchPathsToTry) {
            for (String extension : extensionsToTry) {
                if (!name.endsWith(extension)) {
                    name = name + extension;
                }
                searchSet.add(Path.of(searchPath, name).toString());
            }
        }
        return searchSet;
    }

    @Override
    public List<Content<?>> list() {
        List<Content<?>> foundFiles = new ArrayList<>();

        for (String searchPath : searchPaths) {
            Optional<Path> opath = resolver.resolveDirectory(searchPath);
            Path path = opath.orElseThrow();

            FileCapture capture = new FileCapture();
            for (String name : names) {

                for (String extension : extensions) {
                    if (!extension.startsWith(".")) {
                        extension = "." + extension;
                    }
                    String pattern = name.endsWith(extension) ? name : name + Pattern.quote(extension);
                    RegexPathFilter filter = new RegexPathFilter(pattern);

                    NBIOWalker.walk(path, capture, filter);
                }

            }

            for (Path foundPath : capture) {
                Path fullPath = path.resolve(foundPath);
                foundFiles.add(new PathContent(fullPath));
            }

        }
        return foundFiles;
    }

    private static class RegexPathFilter implements DirectoryStream.Filter<Path> {

        private final Pattern regex;

        public RegexPathFilter(String pattern) {
            this.regex = Pattern.compile(pattern);
        }

        @Override
        public boolean accept(Path entry) throws IOException {
            String input = entry.toString();
            Matcher matcher = regex.matcher(input);
            boolean matches = matcher.matches();
            return matches;
        }

        public String toString() {
            return regex.toString();
        }
    }

    private static class FileCapture implements NBIOWalker.PathVisitor, Iterable<Path> {
        List<Path> found = new ArrayList<>();

        @Override
        public void visit(Path foundPath) {
            found.add(foundPath);
        }

        @Override
        public Iterator<Path> iterator() {
            return found.iterator();
        }
    }


}
