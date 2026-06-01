/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.api.docsapi.docexporter;

import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

@Command(name = "export-docs", description = "Export the bundled documentation to a zip file.")
@Service(value=BundledApp.class,selector = "export-docs")
public class BundledMarkdownExporter implements BundledApp {

    @Option(names = "--zipfile", paramLabel = "<file>", defaultValue = "exported_docs.zip",
        description = "zip file to write to (default: ${DEFAULT-VALUE})")
    private String zipfile;

    @Option(names = {"--help", "-h", "-?"}, usageHelp = true, description = "Display help")
    private boolean helpRequested;

    public static void main(String[] args) {
        new BundledMarkdownExporter().applyAsInt(args);

    }

    /// Exposes the picocli model so the `runapp` command-stream verb can adapt
    /// `name=value` parameters into this app's argv (e.g. `zipfile=x` -> `--zipfile=x`).
    @Override
    public Optional<CommandLine> getCommandModel() {
        return Optional.of(new CommandLine(this));
    }

    @Override
    public int applyAsInt(String[] args) {
        CommandLine cmd = new CommandLine(this);
        try {
            cmd.parseArgs(args);
        } catch (CommandLine.ParameterException pe) {
            System.err.println(pe.getMessage());
            cmd.usage(System.err);
            return 1;
        }
        if (helpRequested) {
            cmd.usage(System.out);
            return 0;
        }

        new BundledMarkdownZipExporter(new BundledFrontmatterInjector(1000,100)).exportDocs(Path.of(zipfile));

        return 0;
    }
}
