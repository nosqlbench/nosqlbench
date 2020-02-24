package io.nosqlbench.docsys.api;

import io.nosqlbench.docsys.core.DocsysDynamicService;

/**
 * At runtime, any instances of this service will be used to find
 * paths to be shared via the {@link DocsysDynamicService}.
 */
public interface DocsysDynamicManifest {
    DocsBinder getDocs();
}
