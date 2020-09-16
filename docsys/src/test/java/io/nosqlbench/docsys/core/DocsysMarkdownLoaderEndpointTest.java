package io.nosqlbench.docsys.core;

import io.nosqlbench.docsys.endpoints.DocsysMarkdownEndpoint;
import org.junit.Test;

public class DocsysMarkdownLoaderEndpointTest {

    @Test
    public void testDocLoader() {
        DocsysMarkdownEndpoint ep = new DocsysMarkdownEndpoint();
        String markdownList = ep.getMarkdownList(true);
    }

}
