package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.markdown.providers.DocsRootDirectory;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSource;

@Service(RawMarkdownSource.class)
public class DocsForTestingOnly extends DocsRootDirectory {

    @Override
    protected String getRootPathName() {
        return "docs-for-testing-only";
    }

}
