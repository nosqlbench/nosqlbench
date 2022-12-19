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

package io.nosqlbench.api.docsapi.docexporter;

import io.nosqlbench.api.spi.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service(value=BundledApp.class,selector = "export-docs")
public class BundledMarkdownExporter implements BundledApp {

    public static void main(String[] args) {
        new BundledMarkdownExporter().applyAsInt(args);

    }
    @Override
    public int applyAsInt(String[] args) {
        final OptionParser parser = new OptionParser();

        OptionSpec<String> zipfileSpec = parser.accepts("zipfile", "zip file to write to")
            .withOptionalArg().ofType(String.class).defaultsTo("exported_docs.zip");

        OptionSpec<?> helpSpec = parser.acceptsAll(List.of("help", "h", "?"), "Display help").forHelp();
        OptionSet options = parser.parse(args);
        if (options.has(helpSpec)) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                throw new RuntimeException("Unable to show help:" + e);
            }
        }

        String zipfile = options.valueOf(zipfileSpec);

        new BundledMarkdownZipExporter(new BundledFrontmatterInjector()).exportDocs(Path.of(zipfile));

        return 0;
    }
}
