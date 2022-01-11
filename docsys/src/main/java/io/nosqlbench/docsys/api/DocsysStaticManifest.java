package io.nosqlbench.docsys.api;

import io.nosqlbench.docapi.DocsBinder;

/**
 * At runtime, any instances of this service will be used to find
 * paths to be hosted as static content.
 */
public interface DocsysStaticManifest {
    DocsBinder getDocs();
}
