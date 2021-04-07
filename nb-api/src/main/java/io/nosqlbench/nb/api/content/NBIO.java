package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.*;
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
 * much in NoSqlBench after workload initialization, so is not performance oriented
 *
 */
public class NBIO implements NBPathsAPI.Facets {

    private static String[] globalIncludes = new String[0];

    public synchronized static void addGlobalIncludes(String[] globalIncludes) {
        NBIO.globalIncludes = globalIncludes;
    }

    private URIResolver resolver;

    private List<String> names = new ArrayList<>();
    private List<String> extensions = new ArrayList<>();
    private List<String> prefixes = Arrays.asList(globalIncludes);

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

    public static List<String> readLines(String filename) {
        Content<?> data = NBIO.all().prefix("data").name(filename).first().orElseThrow(
                () -> new BasicError("Unable to read lines from " + filename)
        );
        String[] split = data.getCharBuffer().toString().split("\n");
        return Arrays.asList(split);
    }

    public static CSVParser readFileCSV(String filename, String... searchPaths) {
        return NBIO.readFileDelimCSV(filename, ',', searchPaths);
    }

    public static CSVParser readFileDelimCSV(String filename,char delim, String... searchPaths) {
        Reader reader = NBIO.readReader(filename, searchPaths);
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delim).withFirstRecordAsHeader();
        try {
            CSVParser parser = new CSVParser(reader, format);
            return parser;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static InputStream readInputStream(String filename, String... searchPaths) {
        return NBIO.all().prefix(searchPaths).name(filename).one().getInputStream();
    }

    private static Reader readReader(String filename, String... searchPaths) {
        return NBIO.all().prefix(searchPaths).name(filename).one().getReader();
    }

    public static CharBuffer readCharBuffer(String fileName, String... searchPaths) {
        return NBIO.all().prefix(searchPaths).name(fileName).one().getCharBuffer();
    }

    public static Path getFirstLocalPath(String... potentials) {
        Optional<Content<?>> first = NBIO.local().name(potentials).first();
        return first.orElseThrow(
                () -> new BasicError("Unable to find loadable content at " + String.join(",",potentials))
        ).asPath();
    }

    public static Optional<Path> findFirstLocalPath(String... potentials) {
        Optional<Content<?>> first = NBIO.local().name(potentials).first();
        Optional<Path> path = first.map(Content::asPath);
        return path;
    }

    public static InputStream readInputStream(String fromPath, String yaml, String[] searchPaths) {
        return null;
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
        if (list.size() > 0) {
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
    public Content<?> one() {


        List<Content<?>> list = list();
        if (list.size() == 0) {
            throw new BasicError("Unable to find even a single source for '" + this.toString() + "'");
        }

        if (list.size() > 1) {
            String found = list.stream().map(c -> c.getURI().toString()).collect(Collectors.joining(","));
            throw new BasicError(("Found too many sources for '" + this.toString() + "', ambiguous name. Pick from " + found));
        }
        return list.get(0);

    }

    @Override
    public List<List<Content<?>>> resolveEach() {
        List<List<Content<?>>> resolved = new ArrayList<>();
        for (String name : names) {
            LinkedHashSet<String> slotSearchPaths = expandNamesAndSuffixes(List.of(name), extensions);
            Content<?> content = null;
            for (String slotSearchPath : slotSearchPaths) {
                List<Content<?>> contents = resolver.resolve(slotSearchPath);
                resolved.add(contents);
            }
        }
        return resolved;
    }


    // for testing
    public LinkedHashSet<String> expandNamesAndSuffixes() {
        return expandNamesAndSuffixes(names, extensions);
    }


    // for testing
    public LinkedHashSet<String> expandNamesAndSuffixes(
        List<String> _names,
        List<String> _suffixes) {

        LinkedHashSet<String> searches = new LinkedHashSet<>();

        if (_names.size()==0 && prefixes.size()==0) {
            searches.add(".*");
        } else if (_names.size()>0 && _suffixes.size()==0) {
            searches.addAll(_names);
        } else if (_names.size()==0 && _suffixes.size()>0) {
            _suffixes.stream().map(s -> ".*"+s).forEach(searches::add);
        } else {
            for (String name : _names) {
                if (!name.equals(".*")) {
                    searches.add(name);
                }
                for (String suffix : _suffixes) {
                    if (!name.endsWith(suffix)) {
                        searches.add(name+suffix);
                    }
                }
            }
        }
        return searches;
    }

    @Override
    public List<Content<?>> list() {
        LinkedHashSet<String> searches = expandNamesAndSuffixes();
        LinkedHashSet<Content<?>> foundFiles = new LinkedHashSet<>();

        // wrap in local search iterator
        for (String search : searches) {
            List<Content<?>> founds = resolver.resolve(search);
            foundFiles.addAll(founds);
        }

        // If this has no names or suffixes included, use a wildcard for all resources found
        // under the respective directory roots for the prefixes
        if (searches.size()==0) {
            searches.add(".*");
        }
        for (String prefix : prefixes) {
            List<Path> founds = resolver.resolveDirectory(prefix);
            NBIOWalker.CollectVisitor capture = new NBIOWalker.CollectVisitor(true,false);


            for (Path path : founds) {
                for (String searchPattern : searches) {
                    NBIOWalker.RegexFilter filter = new NBIOWalker.RegexFilter(searchPattern,true);
//                    RegexPathFilter filter = new RegexPathFilter(searchPattern, true);
                    NBIOWalker.walkFullPath(path, capture, filter);
                }
            }
            capture.get().stream().map(PathContent::new).forEach(foundFiles::add);
        }

        return new ArrayList<>(foundFiles);
    }

    @Override
    public List<Path> relativeTo(String... base) {
        String base1 = base[0];
        String[] rest = new String[base.length-1];
        System.arraycopy(base,1,rest,0,rest.length);

        List<Path> paths = new ArrayList<>();

        List<Content<?>> list = list();
        for (Content<?> c : list) {
            Path path = c.asPath();

            Path fsBase = path.getFileSystem().getPath(base1,rest);
            Path relative = fsBase.relativize(path);
            paths.add(relative);
        }

        return paths;
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
