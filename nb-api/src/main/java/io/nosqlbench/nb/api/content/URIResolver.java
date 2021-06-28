package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.errors.BasicError;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a stateful search object for resources like Paths or URLs.
 * It provides the abilitiy to look for URIs in any form, with simple
 * pluggable search back-ends, in some preferential order.
 */
public class URIResolver implements ContentResolver {

    private List<ContentResolver> loaders = new ArrayList<>();

    private static List<ContentResolver> EVERYWHERE = List.of(
        ResolverForS3.INSTANCE,
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
        loaders.add(ResolverForS3.INSTANCE);
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

    public List<Content<?>> resolve(String uri) {
        return resolve(URI.create(uri));
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        List<Path> dirs = new ArrayList<>();
        for (ContentResolver loader : loaders) {
            dirs.addAll(loader.resolveDirectory(uri));
        }
        return dirs;
    }

    public List<Content<?>> resolve(URI uri) {
        List<Content<?>> resolved = new ArrayList<>();
        for (ContentResolver loader : loaders) {
            List<Content<?>> contents = loader.resolve(uri);
            resolved.addAll(contents);
        }
        return resolved;
    }

    public List<Content<?>> resolveAll(String uri) {
        return resolveAll(URI.create(uri));
    }

    public List<Content<?>> resolveAll(URI uri) {
        List<Content<?>> allFound = new ArrayList<>();
        for (ContentResolver loader : loaders) {
            allFound.addAll(loader.resolve(uri));
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

    public Content<?> resolveOne(String candidatePath) {
        List<Content<?>> contents = resolveAll(candidatePath);
        if (contents.size()==1) {
            return contents.get(0);
        }
        if (contents.size()==0) {
            return null;
        }
        throw new BasicError("Error while loading content '" + candidatePath +"', only one is allowed, but " + contents.size() + " were found");
    }

    public String toString() {
        return "[resolver]";
    }


}
