/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.api.markdown.exporter;

import io.nosqlbench.api.markdown.aggregator.MarkdownDocs;
import io.nosqlbench.api.markdown.types.DocScope;
import io.nosqlbench.api.markdown.types.MarkdownInfo;
import io.nosqlbench.api.spi.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import joptsimple.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service(value = BundledApp.class, selector = "markdown-exporter")
public class MarkdownExporter implements BundledApp, Runnable {

    public static final String APP_NAME = "exporter";
    private Path basePath;
    private Set<DocScope> scopeSet;

    public static void main(String[] args) {
        new MarkdownExporter().appMain(args);
    }

    @Override
    public void run() {
        List<MarkdownInfo> markdownInfos = MarkdownDocs.find(new ArrayList<>(scopeSet).toArray(new DocScope[0]));

    }


    @Override
    public int appMain(String[] args) {
        final OptionParser parser = new OptionParser();

        OptionSpec<String> basedir = parser.accepts("basedir", "base directory to write to")
            .withRequiredArg().ofType(String.class).defaultsTo(".");

        OptionSpec<String> docScopes = parser.accepts("scopes", "scopes of documentation to export")
            .withRequiredArg().ofType(String.class).defaultsTo(DocScope.ANY.toString());

        parser.acceptsAll(List.of("-h", "--help", "help"), "Display help").forHelp();

        OptionSet options = parser.parse(args);

        Path basePath = Path.of(basedir.value(options));
        Set<DocScope> scopeSet = docScopes.values(options).stream().map(DocScope::valueOf).collect(Collectors.toSet());
        this.basePath = basePath;
        this.scopeSet = scopeSet;
        run();
        return 0;
    }
}
