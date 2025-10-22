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

package io.nosqlbench.engine.core.metadata;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.ServiceLoader;

public class MarkdownFinder {
    private static final Logger logger = LogManager.getLogger(MarkdownFinder.class);

    public static Optional<String> forHelpTopic(final String topic) {
        String help = null;
        try {
            help = new MarkdownFinder().forDriverAdapterType(topic);
            return Optional.ofNullable(help);
        } catch (final Exception e) {
            MarkdownFinder.logger.debug("Did not find help topic for activity instance: {}", topic);
        }

        try {
            help = new MarkdownFinder().forResourceMarkdown(topic, "docs/");
            return Optional.ofNullable(help);
        } catch (final Exception e) {
            MarkdownFinder.logger.debug("Did not find help topic for generic markdown file: {}(.md)", topic);
        }

        return Optional.empty();

    }

    public String forResourceMarkdown(final String s, final String... additionalSearchPaths) {
        final Optional<Content<?>> docs = NBIO.local()
            .searchPrefixes("", "./", "docs", "docs/")
            .searchPrefixes(additionalSearchPaths)
            .pathname(s)
            .extensionSet(".md")
            .first();

        return docs.map(Content::asString).orElse(null);
    }

    public String forDriverAdapterType(final String selector) {

        ServiceSelector.of(selector, ServiceLoader.load(DriverAdapterLoader.class)).get()
            .orElseThrow(() -> new BasicError("Unable to find driver for '" + selector + "'"));
        return this.forResourceMarkdown(selector + ".md", "docs/");
    }

    /**
     * @return a sorted list of all markdown topics found at the root of the classpath.
     */
    public static List<String> listRootTopics() {
        Set<String> topics = new TreeSet<>();
        String classPath = System.getProperty("java.class.path");
        if (classPath == null || classPath.isBlank()) {
            MarkdownFinder.logger.debug("java.class.path is empty; unable to enumerate help topics.");
            return new ArrayList<>(topics);
        }

        for (String entry : classPath.split(File.pathSeparator)) {
            collectTopicsFromClasspathEntry(entry, topics);
        }

        return new ArrayList<>(topics);
    }

    private static void collectTopicsFromClasspathEntry(String entry, Set<String> topics) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        Path path = Path.of(entry);
        if (!Files.exists(path)) {
            return;
        }
        Path absolute = path.toAbsolutePath().normalize();
        if (Files.isDirectory(path)) {
            if (isTempDirectory(absolute)) {
                return;
            }
            collectTopics(absolute, topics);
        } else if (entry.endsWith(".jar")) {
            collectTopicsFromJar(absolute, topics);
        }
    }

    private static void collectTopicsFromJar(Path jar, Set<String> topics) {
        URI jarUri = URI.create("jar:" + jar.toUri());
        FileSystem fs = null;
        boolean created = false;
        try {
            try {
                fs = FileSystems.newFileSystem(jarUri, java.util.Map.of());
                created = true;
            } catch (FileSystemAlreadyExistsException exists) {
                fs = FileSystems.getFileSystem(jarUri);
            }
            collectTopics(fs.getPath("/"), topics);
        } catch (IOException e) {
            MarkdownFinder.logger.debug("Unable to scan jar '{}' for topics: {}", jar, e.getMessage());
        } finally {
            if (created && fs != null && fs.isOpen()) {
                try {
                    fs.close();
                } catch (IOException e) {
                    MarkdownFinder.logger.debug("Unable to close jar filesystem '{}': {}", jar, e.getMessage());
                }
            }
        }
    }

    private static boolean isTempDirectory(Path path) {
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp == null || tmp.isBlank()) {
            return false;
        }
        Path tempPath = Path.of(tmp).toAbsolutePath().normalize();
        return path.startsWith(tempPath);
    }

    private static void collectTopics(Path root, Set<String> topics) {
        if (root == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equalsIgnoreCase("topics.md"))
                .forEach(path -> parseTopicFile(path, topics));
        } catch (UncheckedIOException e) {
            MarkdownFinder.logger.debug("Unable to scan for topics under '{}': {}", root, e.getCause().getMessage());
        } catch (IOException e) {
            MarkdownFinder.logger.debug("Unable to scan for topics under '{}': {}", root, e.getMessage());
        } catch (Exception e) {
            MarkdownFinder.logger.debug("Unexpected error while scanning '{}': {}", root, e.getMessage());
        }
    }

    private static void parseTopicFile(Path path, Set<String> topics) {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.substring(1).trim())
                .filter(line -> !line.isEmpty())
                .map(line -> line.endsWith(".md") ? line.substring(0, line.length() - 3) : line)
                .forEach(topics::add);
        } catch (IOException e) {
            MarkdownFinder.logger.debug("Unable to read topics from '{}': {}", path, e.getMessage());
        }
    }
}
