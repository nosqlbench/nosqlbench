/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.nb.api.docsapi.BundledMarkdownLoader;
import io.nosqlbench.nb.api.docsapi.DocsBinder;
import io.nosqlbench.nb.api.docsapi.DocsNameSpace;
import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.VirtDataGenDocsApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Exports bundled markdown documentation directly to a directory structure.
 * This is used to generate documentation in the /docs/ directory for the
 * Zola static site generator.
 */
public class BundledMarkdownDirectoryExporter {

    private static final Logger logger = LogManager.getLogger(BundledMarkdownDirectoryExporter.class);

    private final BundledMarkdownProcessor[] filters;
    private final Function<Path, MutableMarkdown> parser = MutableMarkdown::new;
    private final boolean useTomlFrontMatter;

    public BundledMarkdownDirectoryExporter(boolean useTomlFrontMatter, BundledMarkdownProcessor... filters) {
        this.useTomlFrontMatter = useTomlFrontMatter;
        this.filters = filters;
    }

    /**
     * Export all documentation to the specified output directory.
     *
     * @param outputDir Base output directory (e.g., /docs/)
     */
    public void exportDocs(Path outputDir) {
        try {
            // Ensure output directory exists
            Files.createDirectories(outputDir);

            // Load bundled markdown from all registered DocsBinder services
            DocsBinder docsNameSpaces = BundledMarkdownLoader.loadBundledMarkdown();

            Set<Path> loaded = new HashSet<>();
            for (DocsNameSpace docs_ns : docsNameSpaces) {
                Path namespaceDir = outputDir.resolve(docs_ns.getName());
                Files.createDirectories(namespaceDir);

                for (Path sourcePath : docs_ns) {
                    if (!loaded.contains(sourcePath)) {
                        exportEntry(sourcePath, sourcePath.getParent(), namespaceDir);
                        loaded.add(sourcePath);
                    }
                }
            }

            // Generate and export binding function documentation
            exportBindingFunctions(outputDir.resolve("reference/bindings"));

            logger.info("Documentation exported to: {}", outputDir.toAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("Failed to export documentation to " + outputDir, e);
        }
    }

    /**
     * Export binding function documentation generated from virtdata annotations.
     */
    private void exportBindingFunctions(Path bindingsDir) throws IOException {
        Files.createDirectories(bindingsDir);

        Map<String, StringBuilder> builderMap = new VirtDataGenDocsApp(null).call();

        for (Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
            String filename = entry.getKey();
            StringBuilder fileStringBuilder = entry.getValue();

            MutableMarkdown parsed = new MutableMarkdown(fileStringBuilder.toString());

            // Apply filters (including front matter injection)
            for (BundledMarkdownProcessor filter : this.filters) {
                parsed = filter.apply(parsed);
            }

            // Write to file
            Path outputFile = bindingsDir.resolve(filename);
            String content = useTomlFrontMatter ? parsed.getComposedMarkdownToml() : parsed.getComposedMarkdown();

            Files.writeString(outputFile, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.debug("Exported binding function doc: {}", outputFile);
        }

        logger.info("Exported {} binding function reference files to {}", builderMap.size(), bindingsDir);
    }

    /**
     * Export a single file or directory entry.
     * Handles paths from different file systems (e.g., JAR files).
     */
    private void exportEntry(Path sourcePath, Path sourceRoot, Path targetDir) throws IOException {
        // Get relative path as string to handle cross-filesystem paths
        String relativePathStr = sourceRoot.relativize(sourcePath).toString();
        Path targetPath = targetDir.resolve(relativePathStr);

        if (Files.isDirectory(sourcePath)) {
            Files.createDirectories(targetPath);
            Files.list(sourcePath).forEach(child -> {
                try {
                    exportEntry(child, sourceRoot, targetDir);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to export: " + child, e);
                }
            });
        } else {
            Files.createDirectories(targetPath.getParent());

            if (sourcePath.toString().toLowerCase().endsWith(".md")) {
                // Process markdown files through filters
                MutableMarkdown parsed = parser.apply(sourcePath);
                for (BundledMarkdownProcessor filter : this.filters) {
                    parsed = filter.apply(parsed);
                }

                String content = useTomlFrontMatter ? parsed.getComposedMarkdownToml() : parsed.getComposedMarkdown();
                Files.writeString(targetPath, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // Copy non-markdown files as-is
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
