package io.nosqlbench.nb.api.markdown;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.api.markdown.providers.DocsRootDirectory;
import io.nosqlbench.nb.api.markdown.providers.RawMarkdownSource;

@Service(value = RawMarkdownSource.class, selector = "docs-for-testing", maturity = Maturity.Stable)
public class DocsForTestingOnly extends DocsRootDirectory {

    @Override
    protected String getRootPathName() {
        return "docs-for-testing-only";
    }

}
