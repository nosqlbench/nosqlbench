/*
 * Copyright (c) 2022-2025 nosqlbench
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

package io.nosqlbench.docsys.apps.docinventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service(value = BundledApp.class, selector = "docs-inventory")
public class DocInventoryApp implements BundledApp {

    private static final Set<String> GENERATED_MARKERS = Set.of("target", "cache", "exported_docs", "generated_docs");
    private static final Map<String, String> AUDIENCE_BY_TOP = Map.ofEntries(
        Map.entry("docs", "meta"),
        Map.entry("devdocs", "developer"),
        Map.entry("local", "internal"),
        Map.entry("nb-adapters", "developer"),
        Map.entry("nb-apis", "developer"),
        Map.entry("nb-engine", "developer"),
        Map.entry("nb-virtdata", "developer"),
        Map.entry("nb-docsys", "developer"),
        Map.entry("nb-spectest", "developer"),
        Map.entry("nbr", "developer"),
        Map.entry("nbr-demos", "developer"),
        Map.entry("nb5", "developer"),
        Map.entry("sort_docs", "meta"),
        Map.entry(".github", "meta")
    );
    private static final Set<String> ROOT_USER_FILES = Set.of(
        "README.md",
        "DOWNLOADS.md",
        "PREVIEW_NOTES.md",
        "RELEASE_NOTES.md",
        "BUILDING.md",
        "CONTRIBUTING.md",
        "CODE_OF_CONDUCT.md"
    );

    private static final Set<String> EXCLUDED_SEGMENTS = Set.of(".git", ".github", "local", "cache", "target");
    private static final Set<String> STAGING_TOPS = Set.of("sort_docs");
    private static final Set<String> INTERNAL_TOPS = Set.of("devdocs");

    @Override
    public int applyAsInt(String[] args) {
        Args argv = parseArgs(args);
        if (argv.help()) {
            printUsage();
            return 0;
        }
        try {
            List<DocRecord> docs = scanDocs(argv.root());
            DocInventory report = buildReport(argv.root(), docs);
            writeReport(argv.output(), report);
            System.out.printf(
                Locale.ROOT,
                "Wrote %d entries to %s%n",
                docs.size(),
                argv.output().toAbsolutePath().normalize()
            );
            return 0;
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("docs-inventory: " + e.getMessage());
            return 2;
        }
    }

    public static void main(String[] args) {
        int exit = new DocInventoryApp().applyAsInt(args);
        if (exit != 0) {
            System.exit(exit);
        }
    }

    private Args parseArgs(String[] args) {
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        Path root = cwd;
        Path output = root.resolve("docs/docs_inventory.json");
        boolean help = false;

        List<String> argv = new ArrayList<>(Arrays.asList(args));
        for (int i = 0; i < argv.size(); i++) {
            String word = argv.get(i);
            switch (word) {
                case "-h":
                case "--help":
                    help = true;
                    break;
                case "--root":
                    root = resolveRequiredPath(argv, ++i, "root")
                        .toAbsolutePath()
                        .normalize();
                    if (!Files.isDirectory(root)) {
                        throw new IllegalArgumentException("root path does not exist: " + root);
                    }
                    if (!output.isAbsolute()) {
                        output = root.resolve(output);
                    }
                    break;
                case "-o":
                case "--output":
                    Path candidate = resolveRequiredPath(argv, ++i, "output");
                    output = candidate.isAbsolute() ? candidate : root.resolve(candidate);
                    break;
                default:
                    throw new IllegalArgumentException("unknown argument: " + word);
            }
        }

        return new Args(root, output, help);
    }

    private Path resolveRequiredPath(List<String> argv, int index, String name) {
        if (index >= argv.size()) {
            throw new IllegalArgumentException("missing value for --" + name);
        }
        return Paths.get(argv.get(index));
    }

    private List<DocRecord> scanDocs(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("root path does not exist: " + root);
        }
        List<DocRecord> docs;
        try (Stream<Path> stream = Files.walk(root)) {
            docs = stream
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                .filter(p -> !containsExcludedSegment(p, root))
                .map(p -> toRecord(root, p))
                .sorted(Comparator.comparing(DocRecord::path))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return docs;
    }

    private boolean containsExcludedSegment(Path path, Path root) {
        Path relative = root.relativize(path);
        for (Path part : relative) {
            if (EXCLUDED_SEGMENTS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private DocRecord toRecord(Path root, Path path) {
        Path relative = root.relativize(path);
        String rel = normalize(relative);
        String[] parts = rel.split("/");
        boolean generated = hasGeneratedMarker(parts);
        String owner = parts.length <= 1 ? "root" : parts[0];
        String module = parts.length == 0 ? "root" : parts[0];
        if (module.isBlank()) {
            module = "root";
        }
        String audience = inferAudience(parts, rel);
        DocOrigin origin = new DocOrigin(module, rel, "filesystem");
        String visibility = inferVisibility(rel, parts);
        String curationState = inferCurationState(visibility);
        return new DocRecord(rel, owner, audience, generated, visibility, curationState, origin);
    }

    private boolean hasGeneratedMarker(String[] parts) {
        for (String part : parts) {
            if (GENERATED_MARKERS.contains(part)) {
                return true;
            }
        }
        return false;
    }

    private String inferAudience(String[] parts, String relPath) {
        if (parts.length <= 1) {
            return ROOT_USER_FILES.contains(relPath) ? "user" : "unknown";
        }
        if (ROOT_USER_FILES.contains(relPath)) {
            return "user";
        }
        String top = parts[0];
        if ("local".equals(top) && parts.length > 1 && "nosqlbench-build-docs".equals(parts[1])) {
            return "user";
        }
        if (top.startsWith("nb") && !AUDIENCE_BY_TOP.containsKey(top)) {
            return "developer";
        }
        return AUDIENCE_BY_TOP.getOrDefault(top, "unknown");
    }

    private String inferVisibility(String relPath, String[] parts) {
        if (ROOT_USER_FILES.contains(relPath)) {
            return "public";
        }
        if (parts.length == 0) {
            return "public";
        }
        String top = parts[0];
        if (STAGING_TOPS.contains(top)) {
            return "staging";
        }
        if (INTERNAL_TOPS.contains(top)) {
            return "internal";
        }
        return "public";
    }

    private String inferCurationState(String visibility) {
        return switch (visibility) {
            case "staging" -> "staging";
            case "internal" -> "internal";
            default -> "curated";
        };
    }

    private DocInventory buildReport(Path root, List<DocRecord> docs) {
        Stats stats = new Stats(
            aggregate(docs, DocRecord::owner),
            aggregate(docs, DocRecord::audience),
            aggregate(docs, doc -> doc.generated() ? "generated" : "authored"),
            aggregate(docs, DocRecord::visibility),
            aggregate(docs, DocRecord::curationState)
        );
        return new DocInventory(
            Instant.now().toString(),
            root.toAbsolutePath().normalize().toString(),
            docs.size(),
            stats,
            docs
        );
    }

    private Map<String, Long> aggregate(List<DocRecord> docs, Function<DocRecord, String> classifier) {
        Map<String, Long> grouped = docs.stream()
            .collect(Collectors.groupingBy(classifier, TreeMap::new, Collectors.counting()));
        return new LinkedHashMap<>(grouped);
    }

    private void writeReport(Path output, DocInventory report) throws IOException {
        Path parent = output.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
        writer.writeValue(output.toFile(), report);
    }

    private String normalize(Path relative) {
        return relative.toString().replace('\\', '/');
    }

    private void printUsage() {
        System.out.println(
            String.join(
                System.lineSeparator(),
                "Usage: nb app docs-inventory [--root <path>] [--output <path>]",
                "  --root <path>    Repository root to scan (default: current directory)",
                "  -o, --output     Output file (default: <root>/docs/docs_inventory.json)",
                "  -h, --help       Show this usage text"
            )
        );
    }

    private record Args(Path root, Path output, boolean help) {}

    private record DocOrigin(String module, String path, String extraction) {}

    private record Stats(
        @JsonProperty("by_owner") Map<String, Long> byOwner,
        @JsonProperty("by_audience") Map<String, Long> byAudience,
        @JsonProperty("by_generation") Map<String, Long> byGeneration,
        @JsonProperty("by_visibility") Map<String, Long> byVisibility,
        @JsonProperty("by_curation") Map<String, Long> byCuration
    ) {}

    private record DocInventory(
        @JsonProperty("generated_at") String generatedAt,
        @JsonProperty("root") String root,
        @JsonProperty("doc_count") int docCount,
        Stats stats,
        List<DocRecord> docs
    ) {}

    private record DocRecord(
        String path,
        String owner,
        String audience,
        boolean generated,
        String visibility,
        @JsonProperty("curation_state") String curationState,
        DocOrigin origin
    ) {}
}
