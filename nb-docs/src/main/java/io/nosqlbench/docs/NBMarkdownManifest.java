package io.nosqlbench.docs;

import io.virtdata.annotations.Service;
import io.virtdata.docsys.api.Docs;
import io.virtdata.docsys.api.DocsBinder;
import io.virtdata.docsys.api.DocsysDynamicManifest;

@Service(DocsysDynamicManifest.class)
public class NBMarkdownManifest implements DocsysDynamicManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docs-for-eb")
                .addFirstFoundPath("nb-cli/src/main/resources/docs-for" +
                                "-nb/",
                        "docs-for-nb/")
                .setEnabledByDefault(false)
                .asDocsBinder();
    }
}
