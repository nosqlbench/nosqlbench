package io.nosqlbench.engine.docs;

import io.nosqlbench.docapi.Docs;
import io.nosqlbench.docapi.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DocsysDynamicManifest.class, selector = "docs-for-eb")
public class NosqlbenchMarkdownManifest implements DocsysDynamicManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docs-for-eb")
            .addFirstFoundPath(
                "engine-docs/src/main/resources/docs-for-nb/",
                "docs-for-nb/"
            ).setEnabledByDefault(true)
            .asDocsBinder();
    }
}
