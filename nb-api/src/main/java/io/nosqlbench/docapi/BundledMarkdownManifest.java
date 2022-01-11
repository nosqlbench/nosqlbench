package io.nosqlbench.docapi;

/**
 * At runtime, any instances of markdown content or other static
 * assets which are intended to be copied into the external doc site,
 * or otherwise made available under a local doc service.
 */
public interface BundledMarkdownManifest {
    DocsBinder getDocs();
}
