package io.nosqlbench.nb.api.markdown.aggregator;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownDocsTest {

    @Test
    public void testLoadMarkdown() {
        List<MarkdownInfo> all = MarkdownDocs.findAll();
        assertThat(all).hasSizeGreaterThan(0);

    }

}
