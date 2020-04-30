package io.nosqlbench.virtdata.userlibs.docinfo;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.docsys.api.DocsysDynamicManifest;

@Service(DocsysDynamicManifest.class)
public class VirtdataMarkdownManifest implements DocsysDynamicManifest {

    public DocsBinder getDocs() {
        return new Docs().namespace("virtdata-docs").addFirstFoundPath(
                "virtdata-userlibs/src/main/resources/docs-for-virtdata/",
                "docs-for-virtdata/")
                .setEnabledByDefault(true)
                .asDocsBinder();
    }

}
