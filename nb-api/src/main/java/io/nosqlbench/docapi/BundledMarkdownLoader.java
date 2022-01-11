package io.nosqlbench.docapi;

import java.util.ServiceLoader;

public class BundledMarkdownLoader {

    public static DocsBinder loadBundledMarkdown() {
        ServiceLoader<BundledMarkdownManifest> loader = ServiceLoader.load(BundledMarkdownManifest.class);
        Docs docs = new Docs();
        for (BundledMarkdownManifest docPathInfos : loader) {
            docs.merge(docPathInfos.getDocs());
        }

        return docs;
    }


}
