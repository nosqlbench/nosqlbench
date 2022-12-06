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

import io.nosqlbench.api.docsapi.BundledMarkdownLoader;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.api.docsapi.DocsNameSpace;
import io.nosqlbench.api.markdown.aggregator.MutableMarkdown;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.VirtDataGenDocsApp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BundledMarkdownZipExporter {

    private final BundledMarkdownProcessor[] filters;
    private final Function<Path, MutableMarkdown> parser = MutableMarkdown::new;

    public BundledMarkdownZipExporter(BundledMarkdownProcessor... filters) {
        this.filters = filters;
    }

    public void exportDocs(Path out) {
        ZipOutputStream zipstream;
        try {
            OutputStream stream = Files.newOutputStream(out, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            zipstream = new ZipOutputStream(stream);
            zipstream.setMethod(ZipOutputStream.DEFLATED);
            zipstream.setLevel(9);

            DocsBinder docsNameSpaces = BundledMarkdownLoader.loadBundledMarkdown(); //Loads the drivers under @Service Annotation

            for (DocsNameSpace docs_ns : docsNameSpaces) {
                for (Path p : docs_ns) {
                    addEntry(p, p.getParent(), zipstream, docs_ns.getName() + "/");
                }
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Map<String, StringBuilder>> future = executorService.submit(new VirtDataGenDocsApp(null));
            Map<String, StringBuilder> builderMap = future.get();
            executorService.shutdown();
            String bindingsPrefix ="bindings/";
            for(Map.Entry<String, StringBuilder> entry : builderMap.entrySet())
            {
                String filename = entry.getKey();
                StringBuilder fileStringBuilder = entry.getValue();
                ZipEntry zipEntry = new ZipEntry(bindingsPrefix +filename);
                zipEntry.setTime(new Date().getTime());
                zipstream.putNextEntry(zipEntry);
                zipstream.write(fileStringBuilder.toString().getBytes());
                zipstream.closeEntry();
            }
            zipstream.finish();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addEntry(Path p, Path r, ZipOutputStream zos, String prefix) throws IOException {

        String name = r.relativize(p).toString();
        name = Files.isDirectory(p) ? (name.endsWith(File.separator) ? name : name + File.separator) : name;
        ZipEntry entry = new ZipEntry(prefix + name);

        if (Files.isDirectory(p)) {
            zos.putNextEntry(entry);
            DirectoryStream<Path> stream = Files.newDirectoryStream(p);
            for (Path path : stream) {
                addEntry(path,r,zos, prefix);
            }
        } else {
            entry.setTime(Files.getLastModifiedTime(p).toMillis());
            zos.putNextEntry(entry);

            if (p.toString().toLowerCase(Locale.ROOT).endsWith(".md")) {
                MutableMarkdown parsed = parser.apply(p);
                for (BundledMarkdownProcessor filter : this.filters) {
                    parsed = filter.apply(parsed);
                }
                zos.write(parsed.getComposedMarkdown().getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] bytes = Files.readAllBytes(p);
                zos.write(bytes);
            }
        }
        zos.closeEntry();

    }

}
