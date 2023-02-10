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

package io.nosqlbench.api.content;

import io.nosqlbench.api.errors.BasicError;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * NBIO is a helper utility packaged as a search builder and fluent API. It uses value semantics internally, so it is
 * safe to re-use as a stateful configuration builder for finding files in various ways.
 * <p>
 * Since this is meant to ease development around a usually over-complicated surface area in the JVM (Files, Paths,
 * URIs, accessing data, knowing where it comes from, searching for it, etc), more emphasis was put on ease of use and
 * clarity than efficiency. This set of classes is not expected to be used much in NoSqlBench after workload
 * initialization, so is not performance oriented.
 */
public class NBIO implements NBPathsAPI.Facets {

    private static String[] globalIncludes = new String[0];

    public synchronized static void addGlobalIncludes(String[] globalIncludes) {
        NBIO.globalIncludes = globalIncludes;
    }

    private URIResolver resolver;

    private List<String> names = new ArrayList<>();
    //    private List<String> extensions = new ArrayList<>();
    private List<Set<String>> extensionSets = new ArrayList<>();
    private Set<String> prefixes = new HashSet<>(Arrays.asList(globalIncludes));

    private NBIO() {
    }

    private NBIO(URIResolver resolver,
                 Set<String> prefixes,
                 List<String> names,
                 List<Set<String>> extensionSets) {
        this.resolver = resolver;
        this.prefixes = prefixes;
        this.names = names;
        this.extensionSets = extensionSets;
    }

    public static List<String> readLines(String filename) {
        Content<?> data = NBIO.all().searchPrefixes("data").pathname(filename).first().orElseThrow(
            () -> new BasicError("Unable to read lines from " + filename)
        );
        String[] split = data.getCharBuffer().toString().split("\n");
        return Arrays.asList(split);
    }


    public static CSVParser readFileCSV(String filename, String... searchPaths) {
        return NBIO.readFileDelimCSV(filename, ',', searchPaths);
    }

    public static CSVParser readFileDelimCSV(String filename, char delim, String... searchPaths) {
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
        return NBIO.all().searchPrefixes(searchPaths).pathname(filename).one().getInputStream();
    }

    private static Reader readReader(String filename, String... searchPaths) {
        return NBIO.all().searchPrefixes(searchPaths).pathname(filename).one().getReader();
    }

    public static CharBuffer readCharBuffer(String fileName, String... searchPaths) {
        return NBIO.all().searchPrefixes(searchPaths).pathname(fileName).one().getCharBuffer();
    }

    public static Path getFirstLocalPath(String... potentials) {
        Optional<Content<?>> first = NBIO.local().pathname(potentials).first();
        return first.orElseThrow(
            () -> new BasicError("Unable to find loadable content at " + String.join(",", potentials))
        ).asPath();
    }

    public static Optional<Path> findFirstLocalPath(String... potentials) {
        Optional<Content<?>> first = NBIO.local().pathname(potentials).first();
        Optional<Path> path = first.map(Content::asPath);
        return path;
    }

    public static InputStream readInputStream(String fromPath, String yaml, String[] searchPaths) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes localContent() {
        this.resolver = URIResolvers.inFS().inCP();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes remoteContent() {
        this.resolver = URIResolvers.inURLs();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes internalContent() {
        this.resolver = URIResolvers.inClasspath();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes fileContent() {
        this.resolver = URIResolvers.inFS();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes allContent() {
        this.resolver = URIResolvers.inFS().inCP().inURLs();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetPrefixes searchPrefixes(String... searchPaths) {
        HashSet<String> addingPaths = new HashSet<>(this.prefixes);
        addingPaths.addAll(Arrays.asList(searchPaths));
        return new NBIO(resolver, addingPaths, names, extensionSets);
    }

    private final static Pattern extensionPattern = Pattern.compile("\\.[a-zA-Z]+");
    private final static Pattern wildcardsPattern = Pattern.compile(".*?[^?+*][\\?\\+\\*].*");

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.DoSearch search(String... searches) {
        List<String> prefixesToAdd = new ArrayList<>();
        List<String> namesToAdd = new ArrayList<>();
        List<String> extensionsToAdd = new ArrayList<>();

        for (String search : searches) {
            int dotAt = search.lastIndexOf('.');
            String candidateExtension = search.substring(dotAt);
            if (extensionPattern.matcher(candidateExtension).matches()) {
                extensionsToAdd.add(candidateExtension);
                search = search.substring(0, dotAt);
            }

            String[] parts = search.split(File.separator);
            if (parts.length > 0 && search.startsWith(File.separator) && !parts[0].startsWith(File.separator)) {
                // maintain absolute or relative pathing in spite of String::split
                parts[0] = File.separator + parts[0];
            }

            int literalsTill = 0;
            while (true) {
                if (literalsTill >= parts.length - 1) {
                    break;
                }
                if (wildcardsPattern.matcher(parts[literalsTill]).matches()) {
                    break;
                }
                literalsTill++;
            }

            String[] prefixary = new String[literalsTill];
            System.arraycopy(parts, 0, prefixary, 0, prefixary.length);
            String prefix = String.join(File.separator, prefixary);
            prefix = (prefix.isEmpty() ? "./" : prefix);
            prefixesToAdd.add(prefix);

            String[] nameary = new String[parts.length - literalsTill];
            System.arraycopy(parts, literalsTill, nameary, 0, nameary.length);
            String name = String.join(File.separator, nameary);
            namesToAdd.add(name);

        }

        return searchPrefixes(prefixesToAdd.toArray(new String[]{}))
            .pathname(namesToAdd.toArray(new String[]{}))
            .extensionSet(extensionsToAdd.toArray(new String[]{}));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetExtensions pathname(String... searchNames) {
        ArrayList<String> addingNames = new ArrayList<>(this.names);
        addingNames.addAll(Arrays.asList(searchNames));
        return new NBIO(resolver, prefixes, addingNames, extensionSets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetExtensions extensionSet(String... extensions) {
        if (extensions.length==0) {
            return this;
        }
        return this.extensionSets(
            new HashSet<>(
                Arrays.asList(extensions).stream()
                    .map(String::toLowerCase)
                    .map(s -> s.startsWith(".") ? s : "." + s)
                    .collect(Collectors.toList())
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NBPathsAPI.GetExtensions extensionSets(Set<String>... cosets) {
        for (Set<String> coset : cosets) {
            if (coset.size()==0) {
                throw new RuntimeException("Extension co-set can not be empty.");
            }
            for (String entry : coset) {
                String adjusted = (entry.startsWith(".") ? entry : "." + entry).toLowerCase();
                if (coset.add(adjusted)) {
                    coset.remove(entry);
                }
            }

            Set<String> addTo = null;
            for (Set<String> extensionSet : this.extensionSets) {
                Set<String> union = new LinkedHashSet<>(coset);
                for (String entry : coset) {
                    if (extensionSet.contains(entry)) {
                        addTo = extensionSet;
                        break;
                    }
                }
                if (addTo != null) {
                    break;
                }
            }
            if (addTo == null) {
                addTo = new LinkedHashSet<>();
                extensionSets.add(addTo);
            }
            addTo.addAll(coset);
        }
        return this;
    }

    /**
     * Search for named resources everywhere: URLs, filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefixes all() {
        return new NBIO().allContent();
    }

    /**
     * Search for named resources in the classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefixes classpath() {
        return new NBIO().internalContent();
    }

    /**
     * Search for named resources on the filesystem
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefixes fs() {
        return new NBIO().fileContent();
    }

    /**
     * Search for named resources locally: filesystem, classpath
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefixes local() {
        return new NBIO().localContent();
    }

    /**
     * Search for named resources only in URLs
     *
     * @return a builder
     */
    public static NBPathsAPI.GetPrefixes remote() {
        return new NBIO().remoteContent();
    }


    /**
     * {@inheritDoc}
     */
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
            throw new BasicError("Found more than one source for " + this + ", but expected to find one at" +
                " most.");
        }
        throw new RuntimeException("Invalid code, go fix it, this should never happen.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content<?> one() {

        if (extensionSets.size()==0) {
            for (String name : names) {
                Content<?> found = resolver.resolveOne(name);
                if (found != null) {
                    return found;
                }
            }
        } else {
            for (String name : names) {
                Content<?> found = resolver.resolveOne(name);
                if (found != null) {
                    return found;
                }

                for (Set<String> extensionSet : extensionSets) {
                    for (String es : extensionSet) {
                        for (String extension : extensionSet) {
                            found = resolver.resolveOne(name + extension);
                            if (found != null) {
                                return found;
                            }
                        }
                    }
                }
            }
        }

        List<Content<?>> list = list();
        if (list.size() == 0) {
            throw new BasicError("Unable to find even a single source for '" + this + "'");
        }

        if (list.size() > 1) {
            String found = list.stream().map(c -> c.getURI().toString()).collect(Collectors.joining("\n", "\n", "\n"));
            throw new BasicError(("Found too many sources for '" + this + "', ambiguous name. Pick from " + found));
        }
        return list.get(0);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<Content<?>>> resolveEach() {
        List<List<Content<?>>> resolved = new ArrayList<>();
        for (String name : names) {
            LinkedHashSet<String> slotSearchPaths = expandNamesAndSuffixes(this.prefixes, List.of(name), extensionSets);
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
        return expandNamesAndSuffixes(prefixes, names, extensionSets);
    }


    /**
     * Given names and suffixes, expand a list of names which may be valid.
     * If no name is given, then <pre>{@code .*}</pre> is used.
     * If suffixes are given, then all returned results must include at least
     * one of the suffixes. If the name includes one of the suffixes given,
     * then additional suffixes are added to the list of searchable path names.
     *
     * @param _names
     *     base filenames or path fragment, possibly fully-qualified
     * @param _suffixCosetLists
     *     zero or more suffixes, which, if given, imply that one of them must match
     * @return Expanded names of valid filename fragments according to the above rules
     */
    public static LinkedHashSet<String> expandNamesAndSuffixes(
        Set<String> _prefixes,
        List<String> _names,
        List<Set<String>> _suffixCosetLists) {

        LinkedHashSet<String> searches = new LinkedHashSet<>();

        if (_names.size() > 0) {
            if (_suffixCosetLists.size() > 0) { // names & suffixes
                searches.addAll(expandSynonymPaths(_names, _suffixCosetLists));
            } else { // names only
                searches.addAll(_names);
            }
        } else {
            if (_suffixCosetLists.size() > 0) { // suffixes only
                for (Set<String> suffixCoset : _suffixCosetLists) {
                    for (String suffix : suffixCoset) {
                        searches.add(".*" + suffix);
                    }
                }
            } else { // neither
                searches.add(".*");
            }
        }
        if (_prefixes.size() > 0) {
            LinkedHashSet<String> prefixed = new LinkedHashSet<>(searches);
            for (String prefix : _prefixes) {
                for (String search : searches) {
                    prefixed.add(prefix + File.separator + search);
                }
            }
            searches = prefixed;
        }

        return searches;
    }

    public static Set<String> expandSynonymPaths(List<String> names, List<Set<String>> suffixSets) {
        if (suffixSets.size()==0) {
            return new LinkedHashSet<>(names);
        }
        Set<String> newnames = new LinkedHashSet<>();
        for (String name : names) {
            Set<String> matchingSet = null;
            String basename = null;
            for (Set<String> suffixSet : suffixSets) {
                for (String suffix : suffixSet) {
                    if (name.endsWith(suffix)) {
                        matchingSet = suffixSet;
                        basename = name.substring(0, name.length() - suffix.length());
//                        newnames.add(name); // Leave this here, it initializes precedence
                        break;
                    }
                }
                if (basename != null) {
                    break;
                }
            }
            if (basename == null) {
                if (name.contains(".") && !name.equals(".*")) {
//                    newnames.add(name);
                    suffixSets.stream().flatMap(s -> s.stream()).map(s -> name + s).forEach(newnames::add);
                } else {
                    suffixSets.stream().flatMap(s -> s.stream()).map(s -> name + s).forEach(newnames::add);
                }
            } else {
                for (String extension : matchingSet) {
                    newnames.add(basename + extension);
                }
            }
        }

        return newnames;
    }

    /**
     * {@inheritDoc}
     */
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
        if (searches.size() == 0) {
            searches.add(".*");
        }

        for (String prefix : this.prefixes) {
            List<Path> directories = resolver.resolveDirectory(prefix);
            NBIOWalker.CollectVisitor capture = new NBIOWalker.CollectVisitor(true, false);


            for (Path dirPath : directories) {
                for (String searchPattern : searches) {
                    NBIOWalker.PathSuffixFilter filter = new NBIOWalker.PathSuffixFilter(searchPattern);
                    NBIOWalker.walkFullPath(dirPath, capture, filter);
                }
            }
            capture.get().stream().map(PathContent::new).forEach(foundFiles::add);
        }

        return new ArrayList<>(foundFiles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Path> relativeTo(String... base) {
        String base1 = base[0];
        String[] rest = new String[base.length - 1];
        System.arraycopy(base, 1, rest, 0, rest.length);

        List<Path> paths = new ArrayList<>();

        List<Content<?>> list = list();
        for (Content<?> c : list) {
            Path path = c.asPath();

            Path fsBase = path.getFileSystem().getPath(base1, rest);
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


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NBIO{" +
            "resolver=" + resolver +
            ", prefixes=" + prefixes +
            ", names=" + names +
            ", extensionSets=" + extensionSets +
            '}';
    }
}
