package io.nosqlbench.engine.docs;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.markdown.providers.DocsRootDirectory;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSource;

import java.util.List;

@Service(RawMarkdownSource.class)
public class NosqlBenchRawMarkdownSource extends DocsRootDirectory {

    @Override
    protected String getRootPathName() {
        return "docs-for-eb";
    }

}
