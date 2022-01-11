package io.nosqlbench.docapi;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        new BundledMarkdownExporter().exportDocs(Path.of(zipfile));
    }

    private void exportDocs(Path out) {
        ZipOutputStream zipstream;
        try {
            OutputStream stream = Files.newOutputStream(out, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            zipstream = new ZipOutputStream(stream);
            zipstream.setMethod(ZipOutputStream.DEFLATED);
            zipstream.setLevel(9);

            DocsBinder docsNameSpaces = BundledMarkdownLoader.loadBundledMarkdown();
            for (DocsNameSpace docs_ns : docsNameSpaces) {
                for (Path p : docs_ns) {
                    addEntry(p, p.getParent(), zipstream);
                }
            }
            zipstream.finish();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addEntry(Path p, Path r, ZipOutputStream zos) throws IOException {

        String name = r.relativize(p).toString();
        name = Files.isDirectory(p) ? (name.endsWith(File.separator) ? name : name + File.separator) : name;

        ZipEntry entry = new ZipEntry(name);


        if (Files.isDirectory(p)) {
            zos.putNextEntry(entry);
            DirectoryStream<Path> stream = Files.newDirectoryStream(p);
            for (Path path : stream) {
                addEntry(path,r,zos);
            }
            zos.closeEntry();
        } else {
            entry.setTime(Files.getLastModifiedTime(p).toMillis());
            zos.putNextEntry(entry);
            byte[] bytes = Files.readAllBytes(p);
            zos.write(bytes);
            zos.closeEntry();
        }

    }
}
