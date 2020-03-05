package io.nosqlbench.engine.docs;

import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;
import io.nosqlbench.virtdata.annotations.Service;

@Service(DocsysDynamicManifest.class)
public class NBMarkdownManifest implements DocsysDynamicManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docs-for-eb")
                .addFirstFoundPath("engine-docs/src/main/resources/docs-for-nb/",
                        "docs-for-nb/")
                .setEnabledByDefault(false)
                .asDocsBinder();
    }
}
