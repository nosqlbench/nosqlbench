package io.nosqlbench.docapi;

import io.nosqlbench.docexporter.BundledMarkdownExporter;
import org.junit.jupiter.api.Test;

public class BundledMarkdownExporterTest {

    @Test
    public void zipUpDocs() {
        BundledMarkdownExporter.main(new String[0]);
    }

}
