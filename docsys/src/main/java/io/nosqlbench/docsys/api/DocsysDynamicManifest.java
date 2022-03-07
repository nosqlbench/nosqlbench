package io.nosqlbench.docsys.api;

import io.nosqlbench.docapi.DocsBinder;
import io.nosqlbench.docsys.endpoints.DocsysMarkdownEndpoint;

/**
 * At runtime, any instances of this service will be used to find
 * paths to be shared via the {@link DocsysMarkdownEndpoint}.
 */
public interface DocsysDynamicManifest {
    DocsBinder getDocs();
}
