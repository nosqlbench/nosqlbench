package io.virtdata.docsys.core;

import io.virtdata.docsys.api.*;

import java.util.ServiceLoader;

/**
 * The standard way to load and use all of the {@link DocsNameSpaceImpl}
 * instances which are present in the runtime via SPI.
 *
 * This implementation ensures that names space collisions are known.
 */
public class DocsysPathLoader {

    public static DocsBinder loadStaticPaths() {
        ServiceLoader<DocsysStaticManifest> loader = ServiceLoader.load(DocsysStaticManifest.class);
        Docs docs = new Docs();
        for (DocsysStaticManifest docPathInfos : loader) {
            docs.merge(docPathInfos.getDocs());
        }
        return docs;
    }

    public static DocsBinder loadDynamicPaths() {
        ServiceLoader<DocsysDynamicManifest> loader = ServiceLoader.load(DocsysDynamicManifest.class);
        Docs docs = new Docs();
        for (DocsysDynamicManifest docPathInfos : loader) {
            docs.merge(docPathInfos.getDocs());
        }
        return docs;
    }
}
