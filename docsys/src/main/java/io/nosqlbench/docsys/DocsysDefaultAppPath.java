package io.nosqlbench.docsys;

import io.nosqlbench.docapi.Docs;
import io.nosqlbench.docapi.DocsBinder;
import io.nosqlbench.docsys.api.DocsysStaticManifest;

//@Service(DocsysStaticManifest.class)
public class DocsysDefaultAppPath implements DocsysStaticManifest {

    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docsys-default-app").addFirstFoundPath(
                "docsys/src/main/resources/docsys-guidebook/",
                "docsys-guidebook/")
                .setEnabledByDefault(true)
                .asDocsBinder();
    }
}
