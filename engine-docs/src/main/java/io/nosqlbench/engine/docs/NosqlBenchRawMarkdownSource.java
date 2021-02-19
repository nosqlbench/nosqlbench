package io.nosqlbench.engine.docs;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.markdown.providers.DocsRootDirectory;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSource;

@Service(value = RawMarkdownSource.class, selector = "docs-for-eb")
public class NosqlBenchRawMarkdownSource extends DocsRootDirectory {

    @Override
    protected String getRootPathName() {
        return "docs-for-eb";
    }

}
