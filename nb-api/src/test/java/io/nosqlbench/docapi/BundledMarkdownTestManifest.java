package io.nosqlbench.docapi;

import io.nosqlbench.nb.annotations.Service;

@Service(value = BundledMarkdownManifest.class, selector = "bundled-markdown-test-docs")
public class BundledMarkdownTestManifest implements BundledMarkdownManifest {
    @Override
    public DocsBinder getDocs() {
        return new Docs()
            .namespace("bundled-markdown-test-docs")
            .addFirstFoundContentPath(
                "nb-api/src/test/resources/testsite1/",
                "testsite1"
            )
            .asDocsBinder();
    }
}
