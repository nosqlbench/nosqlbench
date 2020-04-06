package io.nosqlbench.nb.api.content;

/**
 * This is a URI-centric content locator for URLs and Paths. This central
 * API is meant to make dealing with content bundling and loading easy,
 * given how convoluted using the standard APIs can be.
 *
 * <H2>Selecting Content Source</H2>
 * <p>
 * You can load content from all sources, or any individual source, from
 * URLs, the (default) filesystem, and the classpath. If you use the {@link #lookEverywhere()}
 * method go get your instance, then they are all included, in the order mentioned above.
 * However, it is possible to search within only one, or in a specific order by calling
 * the {@link #inURLs()}, the {@link #inFS()}, or the {@link #inClasspath()}, methods respectively.
 * For example, to search only in the local filesystem use {@link URIResolvers#inFS()} only.</p>
 *
 * <H2>Reading Content</H2>
 * <p>
 * All of the
 * </p>
 */
public class URIResolvers {

    public static URIResolver lookEverywhere() {
        return new URIResolver().all();
    }

    public static URIResolver inFS() {
        return new URIResolver().inFS();
    }

    public static URIResolver inURLs() {
        return new URIResolver().inURLs();
    }

    public static URIResolver inClasspath() {
        return new URIResolver().inCP();
    }
}
