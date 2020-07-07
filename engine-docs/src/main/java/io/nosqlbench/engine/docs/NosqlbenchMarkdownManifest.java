package io.nosqlbench.engine.docs;

import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;
import io.nosqlbench.nb.annotations.Service;

@Service(DocsysDynamicManifest.class)
public class NosqlbenchMarkdownManifest implements DocsysDynamicManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docs-for-eb")
                .addFirstFoundPath("engine-docs/src/main/resources/docs-for-nb/",
                        "docs-for-nb/")
                .setEnabledByDefault(true)
                .asDocsBinder();
    }
}
