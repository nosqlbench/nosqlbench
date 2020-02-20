package io.virtdata.docsys.api;

import io.virtdata.docsys.core.DocsysDynamicService;

/**
 * At runtime, any instances of this service will be used to find
 * paths to be shared via the {@link DocsysDynamicService}.
 */
public interface DocsysDynamicManifest {
    DocsBinder getDocs();
}
