package io.virtdata.docsys;

import io.virtdata.docsys.api.Docs;
import io.virtdata.docsys.api.DocsBinder;
import io.virtdata.docsys.api.DocsysStaticManifest;

//@Service(DocsysStaticManifest.class)
public class DocsysDefaultAppPath implements DocsysStaticManifest {

    @Override
    public DocsBinder getDocs() {
        return new Docs().namespace("docsys-default-app").addFirstFoundPath(
                "virtdata-docsys/src/main/resources/docsys-guidebook/",
                "docsys-guidebook/")
                .setEnabledByDefault(true)
                .asDocsBinder();
    }
}
