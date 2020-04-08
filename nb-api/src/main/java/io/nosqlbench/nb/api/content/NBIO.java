package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.content.fluent.NBPathsAPI;
import io.nosqlbench.nb.api.errors.BasicError;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private List<String> names = new ArrayList<>();
    private List<String> extensions = new ArrayList<>();
    private List<String> prefixes = new ArrayList<>();

    private NBIO() {
    }

    private NBIO(URIResolver resolver,
                 List<String> prefixes,
                 List<String> names,
                 List<String> extensions) {
        this.resolver = resolver;
        this.prefixes = prefixes;
        this.names = names;
        this.extensions = extensions;
    }

    @Override
    public NBPathsAPI.GetPrefix localContent() {
        this.resolver = URIResolvers.inFS().inCP();
        return this;
    }

    @Override
    public NBPathsAPI.GetPrefix remoteContent() {
        this.resolver = URIResolvers.inURLs();
        return this;
    }

    @Override
    public NBPathsAPI.GetPrefix internalContent() {
        this.resolver = URIResolvers.inClasspath();
        return this;
    }

    @Override
    public NBPathsAPI.GetPrefix fileContent() {
        this.resolver = URIResolvers.inFS();
        return this;
    }

    @Override
    public NBPathsAPI.GetPrefix allContent() {
        this.resolver = URIResolvers.inFS().inCP().inURLs();
        return this;
    }

    @Override
    public NBPathsAPI.GetPrefix prefix(String... searchPaths) {
        ArrayList<String> addingPaths = new ArrayList<>(this.prefixes);
        addingPaths.addAll(Arrays.asList(searchPaths));
        return new NBIO(resolver, addingPaths, names, extensions);
    }

    @Override
    public NBPathsAPI.GetExtension name(String... searchNames) {
        ArrayList<String> addingNames = new ArrayList<>(this.names);
        addingNames.addAll(Arrays.asList(searchNames));
        return new NBIO(resolver, prefixes, addingNames, extensions);
    }

    @Override
    public NBPathsAPI.DoSearch extension(String... extensions) {
        ArrayList<String> addingExtensions = new ArrayList<>(this.extensions);
        for (String addingExtension : extensions) {
            addingExtensions.add(dotExtension(addingExtension));
        }
        return new NBIO(resolver, prefixes, names, addingExtensions);
    }

    /**
     * Search for named resources everywhere: URLs, filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefix all() {
        return new NBIO().allContent();
    }

    /**
     * Search for named resources in the classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefix classpath() {
        return new NBIO().internalContent();
    }

    /**
     * Search for named resources on the filesystem
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefix fs() {
        return new NBIO().fileContent();
    }

    /**
     * Search for named resources locally: filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefix local() {
        return new NBIO().localContent();
    }

    /**
     * Search for named resources only in URLs
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefix remote() {
        return new NBIO().remoteContent();
    }


    @Override
    public Optional<Content<?>> first() {

        List<Content<?>> list = list();
        if (list.size()>0) {
            return Optional.of(list.get(0));
        } else {
            return Optional.empty();
        }

    }

    public Optional<Content<?>> maybeOne() {
        List<Content<?>> list = list();

        if (list.size() > 1) {
            throw new BasicError("Found more than one source for " + this.toString() + ", but expected to find one at" +
                " most.");
        }
        throw new RuntimeException("Invalid code, go fix it, this should never happen.");
    }

    @Override
    public Optional<Content<?>> one() {


        List<Content<?>> list = list();
        if (list.size() == 0) {
            throw new BasicError("Unable to find even a single source for '" + this.toString() + "'");
        }

        if (list.size() > 1) {
            String found = list.stream().map(c -> c.getURI().toString()).collect(Collectors.joining(","));
            throw new BasicError(("Found too many sources for '" + this.toString() + "', ambiguous name. Pick from " + found));
        }
        return Optional.of(list.get(0));

    }

    @Override
    public List<Optional<Content<?>>> resolveEach() {
        List<Optional<Content<?>>> resolved = new ArrayList<>();
        for (String name : names) {
            LinkedHashSet<String> slotSearchPaths = expandSearches(prefixes, List.of(name), extensions, false);
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
        return expandSearches(prefixes, names, extensions, false);
    }


    // for testing
    public LinkedHashSet<String> expandSearches(List<String> thePrefixes, List<String> names,
                                                List<String> suffixes, boolean eachPrefix) {

        List<String> prefixesToSearch = new ArrayList<>(thePrefixes);
        List<String> namesToSearch = new ArrayList<>(names);
        List<String> suffixesToSearch = new ArrayList<>(suffixes);

        if (prefixesToSearch.size() == 0) {
            prefixesToSearch.add("");
        }
        if (namesToSearch.size() == 0) {
            namesToSearch.add(".*");
        }
        if (suffixesToSearch.size() == 0) {
            suffixesToSearch.add("");
        }

        LinkedHashSet<String> searches = new LinkedHashSet<>();

        for (String name : namesToSearch) {
            for (String suffix : suffixesToSearch) {
                String search = name;
                search = (search.endsWith(suffix) ? search : search + suffix);

                if (eachPrefix) {
                    for (String prefix : prefixesToSearch) {
                        String withPrefix = (prefix.isEmpty() ? prefix :
                            prefix + FileSystems.getDefault().getSeparator())
                            + search;
                        searches.add(withPrefix);
                    }
                } else {
                    searches.add(search);
                }
            }
        }

        return searches;
    }

//    // for testing
//    public LinkedHashSet<String> expandSearches(String name) {
//
//        LinkedHashSet<String> searchSet = new LinkedHashSet<>();
//
//        List<String> searchPathsToTry = new ArrayList<>();
//        searchPathsToTry.add("");
//        searchPathsToTry.addAll(prefixes);
//
//        List<String> extensionsToTry = new ArrayList<>();
////        extensionsToTry.add("");
//        extensionsToTry.addAll(extensions);
//
//        for (String searchPath : searchPathsToTry) {
//            for (String extension : extensionsToTry) {
//                if (!name.endsWith(extension)) {
//                    name = name + extension;
//                }
//                searchSet.add(Path.of(searchPath, name).toString());
//            }
//        }
//        return searchSet;
//    }


    @Override
    public List<Content<?>> list() {
        LinkedHashSet<String> searches = expandSearches();

        LinkedHashSet<Content<?>> foundFiles = new LinkedHashSet<>();

        // wrap in local search iterator
        for (String search : searches) {
            Content<?> foundInLocal = resolver.resolve(search);
            if (foundInLocal != null) {
                foundFiles.add(foundInLocal);
            }
        }

        for (String searchPath : prefixes) {
            Optional<Path> opath = resolver.resolveDirectory(searchPath);
            Path path = opath.orElseThrow();

            FileCapture capture = new FileCapture();
            for (String searchPattern : searches) {
                RegexPathFilter filter = new RegexPathFilter(searchPattern, true);
                NBIOWalker.walkFullPath(path, capture, filter);
            }

            for (Path foundPath : capture) {
//                Path fullPath = path.resolve(foundPath);
//                foundFiles.add(new PathContent(fullPath));
                foundFiles.add(new PathContent(foundPath));
            }
        }

        return new ArrayList<>(foundFiles);
    }

    private static String tailmatch(String name) {
        if (!name.startsWith("^") && !name.startsWith(".")) {
            name = ".*" + name;
        }
        return name;
    }

    private static String dotExtension(String extension) {
        return extension.startsWith(".") ? extension : "." + extension;
    }

//    private LinkedHashSet<Pattern> expandSearchPatterns(String name) {
//        LinkedHashSet<Pattern> expanded = new LinkedHashSet<>();
//
//        if (extensions.size()==0) {
//            expanded.add(Pattern.compile(tailmatch(name)));
//        }
//
//        for (String extension : extensions) {
//            extension = dotExtension(extension);
//            String withExtension = name.endsWith(extension) ? name : name + Pattern.quote(extension);
//            withExtension=tailmatch(withExtension);
//            Pattern pattern = Pattern.compile(withExtension);
//            expanded.add(pattern);
//        }
//        return expanded;
//    }

    private static class RegexPathFilter implements DirectoryStream.Filter<Path> {

        private final Pattern regex;

        public RegexPathFilter(String pattern, boolean rightglob) {
            if (rightglob && !pattern.startsWith("^") && !pattern.startsWith(".")) {
                this.regex = Pattern.compile(".*" + pattern);
            } else {
                this.regex = Pattern.compile(pattern);
            }
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

    @Override
    public String toString() {
        return "NBIO{" +
            "resolver=" + resolver +
            ", prefixes=" + prefixes +
            ", names=" + names +
            ", extensions=" + extensions +
            '}';
    }
}
