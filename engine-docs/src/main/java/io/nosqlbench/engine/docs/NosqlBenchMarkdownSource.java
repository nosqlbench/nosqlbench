package io.nosqlbench.engine.docs;

import io.nosqlbench.docsys.api.Docs;
import io.nosqlbench.docsys.api.DocsBinder;
import io.nosqlbench.nb.api.annotations.Service;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.content.NBIOWalker;
import io.nosqlbench.nb.api.content.PathContent;
import io.nosqlbench.nb.api.markdown.providers.MarkdownProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service(MarkdownProvider.class)
public class NosqlBenchMarkdownSource implements MarkdownProvider {

    @Override
    public List<Content<?>> getMarkdownInfo() {
        Path docspath = NBIO.local().name("docs-for-eb")
            .one().asPath();
        NBIOWalker.CollectVisitor capture = new NBIOWalker.CollectVisitor(true, false);
        NBIOWalker.RegexFilter filter = new NBIOWalker.RegexFilter("\\.md",true);
        NBIOWalker.walkFullPath(docspath,capture,filter);

        List<Content<?>> content = new ArrayList<>();
        for (Path path : capture.get()) {
            content.add(new PathContent(path));
        }
        return content;

    }
}
