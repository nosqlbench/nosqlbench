package io.nosqlbench.nb.api.markdown.exporter;

import io.nosqlbench.nb.api.markdown.aggregator.DocScope;
import io.nosqlbench.nb.api.markdown.aggregator.MarkdownDocs;
import io.nosqlbench.nb.api.markdown.aggregator.MarkdownInfo;
import joptsimple.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MarkdownExporter implements Runnable {

    public static final String APP_NAME = "exporter";
    private final Path basePath;
    private Set<DocScope> scopeSet;

    public MarkdownExporter(Path basePath, Set<DocScope> scopeSet) {
        this.basePath = basePath;
        this.scopeSet = scopeSet;
    }

    public static void main(String[] args) {
        final OptionParser parser = new OptionParser();

        OptionSpec<String> basedir = parser.accepts("basedir", "base directory to write to")
            .withRequiredArg().ofType(String.class).defaultsTo(".");

        OptionSpec<String> docScopes = parser.accepts("scopes", "scopes of documentation to export")
            .withRequiredArg().ofType(String.class).defaultsTo(DocScope.ANY.toString());

        parser.acceptsAll(List.of("-h","--help","help"),"Display help").forHelp();

        OptionSet options = parser.parse(args);

        Path basePath = Path.of(basedir.value(options));
        Set<DocScope> scopeSet = docScopes.values(options).stream().map(DocScope::valueOf).collect(Collectors.toSet());


        new MarkdownExporter(basePath,scopeSet).run();
    }

    @Override
    public void run() {
        List<MarkdownInfo> markdownInfos = MarkdownDocs.find(new ArrayList<>(scopeSet).toArray(new DocScope[0]));

    }


}
