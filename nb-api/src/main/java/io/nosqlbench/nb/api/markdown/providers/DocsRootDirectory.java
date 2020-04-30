package io.nosqlbench.nb.api.markdown.providers;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.content.NBIOWalker;
import io.nosqlbench.nb.api.content.PathContent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class DocsRootDirectory implements RawMarkdownSource {

    @Override
    public List<Content<?>> getMarkdownInfo() {
        List<Content<?>> list = NBIO.local().name(getRootPathName()).list();
        NBIOWalker.CollectVisitor capture = new NBIOWalker.CollectVisitor(true, false);
        NBIOWalker.RegexFilter filter = new NBIOWalker.RegexFilter("\\.md",true);
        for (Content<?> content : list) {
            Path path = content.asPath();
            NBIOWalker.walkFullPath(path,capture,filter);
        }
        List<Content<?>> content = new ArrayList<>();
        for (Path path : capture.get()) {
            content.add(new PathContent(path));
        }
        return content;

    }

    protected abstract String getRootPathName();

}
