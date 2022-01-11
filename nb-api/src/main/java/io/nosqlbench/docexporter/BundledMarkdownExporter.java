package io.nosqlbench.docexporter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BundledMarkdownExporter {

    public static void main(String[] args) {

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
    }

}
