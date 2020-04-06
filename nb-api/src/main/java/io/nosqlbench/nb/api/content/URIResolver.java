package io.nosqlbench.nb.api.content;

import org.apache.commons.math3.FieldElement;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is a stateful search object for resources like Paths or URLs.
 * It provides the abilitiy to look for URIs in any form, with simple
 * pluggable search back-ends, in some preferential order.
 */
public class URIResolver implements ContentResolver {

    private List<ContentResolver> loaders = new ArrayList<>();

    private static List<ContentResolver> EVERYWHERE = List.of(
        ResolverForURL.INSTANCE,
        ResolverForFilesystem.INSTANCE,
        ResolverForClasspath.INSTANCE
    );

    private List<String> extensions;
    private List<Path> extraPaths;

    public URIResolver() {
    }

    /**
     * Include resources from all known places, including remote URLs,
     * the local default filesystem, and the classpath, which includes
     * the jars that hold the current runtime application.
     * @return this URISearch
     */
    public URIResolver all() {
        loaders = EVERYWHERE;
        return this;
    }

    /**
     * Includ resources in the default filesystem
     * @return this URISearch
     */
    public URIResolver inFS() {
        loaders.add(ResolverForFilesystem.INSTANCE);
        return this;
    }

    /**
     * Include resources in remote URLs
     * @return this URISearch
     */
    public URIResolver inURLs() {
        loaders.add(ResolverForURL.INSTANCE);
        return this;
    }

    /**
     * Include resources within the classpath.
     * @return this URISearch
     */
    public URIResolver inCP() {
        loaders.add(ResolverForClasspath.INSTANCE);
        return this;
    }

    public Optional<Content<?>> resolveOptional(String uri) {
        return Optional.ofNullable(resolve(uri));
    }

    public Content<?> resolve(String uri) {
        return resolve(URI.create(uri));
    }

    @Override
    public Optional<Path> resolveDirectory(URI uri) {
        for (ContentResolver loader : loaders) {
            Optional<Path> path = loader.resolveDirectory(uri);
            if (path.isPresent()) {
                return path;
            }
        }
        return Optional.empty();
    }

    public Content<?> resolve(URI uri) {
        Content<?> resolved = null;
        for (ContentResolver loader : loaders) {
            resolved = loader.resolve(uri);
            if (resolved!=null) {
                break;
            }
        }
        return resolved;
    }

    public List<Content<?>> resolveAll(String uri) {
        return resolveAll(URI.create(uri));
    }

    public List<Content<?>> resolveAll(URI uri) {
        List<Content<?>> allFound = new ArrayList<>();
        for (ContentResolver loader : loaders) {
            Content<?> found = loader.resolve(uri);
            if (found!=null) {
                allFound.add(found);
            }
        }
        return allFound;
    }

    public URIResolver extension(String extension) {
        this.extensions = this.extensions==null ? new ArrayList<>() : this.extensions;
        this.extensions.add(extension);
        return this;
    }

    public URIResolver extraPaths(String extraPath) {
        this.extraPaths = this.extraPaths==null ? new ArrayList<>() : this.extraPaths;
        this.extraPaths.add(Path.of(extraPath));
        return this;
    }
}
