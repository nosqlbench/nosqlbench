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

package io.nosqlbench.docsys.apps.doclint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(value = BundledApp.class, selector = "docs-lint")
public class DocLintApp implements BundledApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Load YAML_LOADER = new Load(LoadSettings.builder().build());
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();

    private static final Set<String> REQUIRED_KEYS = Set.of(
        "title", "audience", "diataxis", "component", "topic", "status", "tags"
    );
    private static final Set<String> VALID_AUDIENCE = Set.of("user", "operator", "developer", "internal", "meta");
    private static final Set<String> VALID_DIATAXIS = Set.of("tutorial", "howto", "explanation", "reference");
    private static final Set<String> VALID_COMPONENT = Set.of("core", "drivers", "virtdata", "nbr", "nb5", "docsys", "site", "community");
    private static final Set<String> VALID_TOPIC = Set.of("workloads", "drivers", "bindings", "metrics", "architecture", "releases", "contributing", "ops", "api", "docops");
    private static final Set<String> VALID_STATUS = Set.of("draft", "review", "live", "deprecated");
    private static final Set<String> TOPICS_REQUIRING_ANCHORS = Set.of("metrics", "api", "architecture");
    private static final List<Path> EXCLUDED_LINK_ROOTS = List.of(
        Paths.get("local"),
        Paths.get("sort_docs"),
        Paths.get("target")
    );
    private static final List<String> FRONTMATTER_PATH_PREFIXES = List.of(
        "docs/",
        "devdocs/",
        "nb-adapters/",
        "nb-apis/",
        "nb-engine/",
        "nb-virtdata/",
        "nbr/",
        "nb5/"
    );
    private static final Pattern REFERENCE_DEFINITION = Pattern.compile("(?m)^\\s{0,3}\\[([^\\]]+)]\\s*:\\s*\\S+.*$");
    private static final Pattern REFERENCE_LINK = Pattern.compile("!?\\[[^\\]]+]\\[([^\\]]+)]");
    private static final Pattern REFERENCE_COLLAPSED = Pattern.compile("!?\\[([^\\]]+)]\\[\\]");

    @Override
    public int applyAsInt(String[] args) {
        Args parsed = parseArgs(args);
        if (parsed.help()) {
            printUsage();
            return 0;
        }

        try {
            JsonNode inventory = MAPPER.readTree(parsed.inventory().toFile());
            Path repoRoot = resolveRepoRoot(inventory.path("root").asText());
            LintResult result = lint(inventory, repoRoot);
            if (!result.messages().isEmpty()) {
                writeJsonReport(parsed.output(), result);
                System.err.println("docs-lint: found " + result.messages().size() + " issue(s). See " +
                    parsed.output().toAbsolutePath().normalize());
                return 2;
            }
            writeJsonReport(parsed.output(), result);
            System.out.printf(Locale.ROOT, "docs-lint: %d docs verified via %s (report: %s)%n",
                inventory.path("doc_count").asInt(),
                parsed.inventory().toAbsolutePath().normalize(),
                parsed.output().toAbsolutePath().normalize());
            return 0;
        } catch (IOException e) {
            System.err.println("docs-lint: " + e.getMessage());
            return 2;
        }
    }

    private LintResult lint(JsonNode inventory, Path repoRoot) {
        List<String> errors = new ArrayList<>();
        Map<String, List<String>> byFile = new LinkedHashMap<>();
        Map<String, Set<String>> linkGraph = new LinkedHashMap<>();
        JsonNode docs = inventory.path("docs");
        if (!docs.isArray()) {
            errors.add("inventory docs field is missing or not an array");
            return new LintResult(errors, Map.of(), Map.of(), Set.of());
        }
        Set<String> managedPaths = new HashSet<>();
        docs.forEach(doc -> managedPaths.add(doc.path("path").asText()));

        Map<Path, Set<String>> anchorCache = new HashMap<>();
        for (JsonNode doc : docs) {
            String path = doc.path("path").asText();
            List<String> docErrors = new ArrayList<>();
            String curationState = doc.path("curation_state").asText("curated");
            if ("staging".equals(curationState)) {
                docErrors.add(path + " remains in staging. Migrate it into docs/ or mark it internal before merging.");
                errors.addAll(docErrors);
                byFile.put(path, docErrors);
                continue;
            }
            Path absolute = repoRoot.resolve(path).normalize();
            if (!Files.exists(absolute)) {
                String missing = "missing file: " + path;
                docErrors.add(missing);
                errors.add(missing);
            }
            String originPath = doc.path("origin").path("path").asText();
            if (!originPath.equals(path)) {
                docErrors.add(path + " origin.path mismatch: " + originPath);
            }
            String originModule = doc.path("origin").path("module").asText();
            String expectedModule = deriveModule(path);
            if (expectedModule != null && !expectedModule.equals(originModule)) {
                docErrors.add(path + " origin.module mismatch: " + originModule + ", expected " + expectedModule);
            }
            String content;
            try {
                content = Files.readString(absolute, StandardCharsets.UTF_8);
            } catch (IOException e) {
                docErrors.add(path + " I/O error: " + e.getMessage());
                errors.addAll(docErrors);
                byFile.put(path, docErrors);
                continue;
            }
            if (requiresFrontMatter(path)) {
                FrontMatter fm = parseFrontMatter(content);
                docErrors.addAll(validateFrontMatter(fm, path));
                docErrors.addAll(validateLinks(path, absolute, repoRoot, fm.body(), managedPaths, anchorCache, linkGraph));
                docErrors.addAll(validateReferenceLinks(path, fm.body()));
                docErrors.addAll(validateAnchors(path, fm.body()));
            }
            if (!docErrors.isEmpty()) {
                errors.addAll(docErrors);
                byFile.put(path, docErrors);
            }
        }

        Map<String, Set<String>> inbound = new LinkedHashMap<>();
        linkGraph.forEach((source, targets) -> targets.forEach(target ->
            inbound.computeIfAbsent(target, k -> new LinkedHashSet<>()).add(source)
        ));

        Set<String> orphans = new LinkedHashSet<>();
        for (String path : managedPaths) {
            if (!requiresFrontMatter(path)) {
                continue;
            }
            Set<String> incoming = inbound.getOrDefault(path, Set.of());
            if (incoming.isEmpty()) {
                orphans.add(path);
            }
        }

        return new LintResult(errors, byFile, linkGraph, orphans);
    }

    private List<String> validateFrontMatter(FrontMatter fm, String displayPath) {
        List<String> errors = new ArrayList<>();
        try {
            if (!fm.present()) {
                errors.add(displayPath + " missing YAML front matter");
                return errors;
            }
            Map<String, Object> map = fm.values();
            Map<String, Object> merged = new LinkedHashMap<>(map);
            Object extra = map.get("extra");
            if (extra instanceof Map<?, ?> extraMap) {
                for (Map.Entry<?, ?> entry : extraMap.entrySet()) {
                    Object key = entry.getKey();
                    if (key instanceof String sk && !merged.containsKey(sk)) {
                        merged.put(sk, entry.getValue());
                    }
                }
            }
            for (String key : REQUIRED_KEYS) {
                if (!merged.containsKey(key)) {
                    errors.add(displayPath + " missing front matter key: " + key);
                }
            }
            String audience = stringValue(merged.get("audience"));
            if (audience == null || !VALID_AUDIENCE.contains(audience)) {
                errors.add(displayPath + " invalid audience: " + audience);
            }
            String diataxis = stringValue(merged.get("diataxis"));
            if (diataxis == null || !VALID_DIATAXIS.contains(diataxis)) {
                errors.add(displayPath + " invalid diataxis: " + diataxis);
            }
            String component = stringValue(merged.get("component"));
            if (component == null || !VALID_COMPONENT.contains(component)) {
                errors.add(displayPath + " invalid component: " + component);
            }
            String topic = stringValue(merged.get("topic"));
            if (topic == null || !VALID_TOPIC.contains(topic)) {
                errors.add(displayPath + " invalid topic: " + topic);
            }
            String status = stringValue(merged.get("status"));
            if (status == null || !VALID_STATUS.contains(status)) {
                errors.add(displayPath + " invalid status: " + status);
            }
            Object tags = merged.get("tags");
            if (!(tags instanceof List<?> tagList) || tagList.isEmpty()) {
                errors.add(displayPath + " tags must be a non-empty list");
            }
        } catch (RuntimeException e) {
            errors.add(displayPath + " front matter parse error: " + e.getMessage());
        }
        return errors;
    }

    private List<String> validateAnchors(String displayPath, String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        Map<String, Integer> anchorCounts = parseAnchorCounts(body);
        Set<String> anchors = anchorCounts.keySet();

        boolean requireAnchors = fileRequiresAnchors(displayPath);

        String markdown = body;
        Node document = MARKDOWN_PARSER.parse(markdown);
        traverse(document, url -> {
            if (url == null || url.isBlank()) {
                return;
            }
            String trimmed = url.trim();
            if (trimmed.startsWith("#")) {
                String anchor = trimmed.substring(1);
                if (anchor.isEmpty()) {
                    errors.add(displayPath + " contains empty anchor in link: " + url);
                    return;
                }
                if (requireAnchors && !anchors.contains(anchor)) {
                    errors.add(displayPath + " missing in-page heading for anchor: #" + anchor);
                }
                return;
            }
            int hash = trimmed.indexOf('#');
            if (hash > 0 && !trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("mailto:")) {
                String anchor = trimmed.substring(hash + 1);
                if (anchor.isEmpty()) {
                    errors.add(displayPath + " contains empty anchor in link: " + url);
                    return;
                }
                if (!anchors.contains(anchor) && requireAnchors) {
                    errors.add(displayPath + " missing heading for anchor: #" + anchor);
                }
            }
        });
        return errors;
    }

    private List<String> validateReferenceLinks(String displayPath, String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        String text = stripCodeSegments(body);
        Set<String> definitions = new LinkedHashSet<>();
        Matcher defMatcher = REFERENCE_DEFINITION.matcher(text);
        while (defMatcher.find()) {
            String label = normalizeReferenceLabel(defMatcher.group(1));
            if (!label.isEmpty()) {
                definitions.add(label);
            }
        }

        Set<String> referenced = new LinkedHashSet<>();
        Matcher refMatcher = REFERENCE_LINK.matcher(text);
        while (refMatcher.find()) {
            String label = normalizeReferenceLabel(refMatcher.group(1));
            if (!label.isEmpty()) {
                referenced.add(label);
            }
        }
        Matcher collapsedMatcher = REFERENCE_COLLAPSED.matcher(text);
        while (collapsedMatcher.find()) {
            String label = normalizeReferenceLabel(collapsedMatcher.group(1));
            if (!label.isEmpty()) {
                referenced.add(label);
            }
        }

        List<String> errors = new ArrayList<>();
        for (String label : referenced) {
            if (!definitions.contains(label)) {
                errors.add(displayPath + " missing reference-style definition for [" + label + "]");
            }
        }
        return errors;
    }

    private String stripCodeSegments(String body) {
        String withoutFences = body
            .replaceAll("(?s)```.*?```", " ")
            .replaceAll("(?s)~~~.*?~~~", " ");
        String withoutIndented = withoutFences.replaceAll("(?m)^(?: {4}|\\t).*$", " ");
        return withoutIndented.replaceAll("`[^`]*`", " ");
    }

    private String normalizeReferenceLabel(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ");
    }

    private boolean fileRequiresAnchors(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        for (String topic : TOPICS_REQUIRING_ANCHORS) {
            if (path.contains(topic)) {
                return true;
            }
        }
        return false;
    }

    private List<String> validateLinks(String displayPath, Path absoluteFile, Path repoRoot, String body, Set<String> managedPaths, Map<Path, Set<String>> anchorCache, Map<String, Set<String>> linkGraph) {
        List<String> errors = new ArrayList<>();
        String markdown = body == null ? "" : body;
        Node document = MARKDOWN_PARSER.parse(markdown);
        traverse(document, url -> {
            if (url == null || url.isBlank()) {
                errors.add(displayPath + " contains empty link target");
                return;
            }
            String sanitized = sanitizeUrl(url);
            if (sanitized == null) {
                return;
            }
            Path resolved = resolveLinkTarget(sanitized, absoluteFile, repoRoot);
            if (resolved == null) {
                errors.add(displayPath + " link escapes repository: " + url);
                return;
            }
            if (isUnderExcludedRoots(resolved, repoRoot)) {
                errors.add(displayPath + " link points into excluded/staging path: " + url);
                return;
            }
            if (!Files.exists(resolved)) {
                errors.add(displayPath + " broken link: " + url);
                return;
            }
            if (sanitized.toLowerCase(Locale.ROOT).endsWith(".md")) {
                String rel = repoRoot.relativize(resolved).toString().replace('\\', '/');
                if (!managedPaths.contains(rel)) {
                    errors.add(displayPath + " link targets unmanaged markdown: " + url + " -> " + rel);
                    return;
                }
                linkGraph.computeIfAbsent(displayPath, k -> new LinkedHashSet<>()).add(rel);
                String anchor = extractAnchor(url);
                if (anchor != null && !anchor.isEmpty()) {
                    Set<String> anchors = computeAnchors(resolved, anchorCache);
                    if (!anchors.contains(anchor)) {
                        errors.add(displayPath + " missing target heading for anchor #" + anchor + " in " + rel);
                    }
                }
            }
        });
        return errors;
    }

    private Set<String> computeAnchors(Path targetFile, Map<Path, Set<String>> anchorCache) {
        return anchorCache.computeIfAbsent(targetFile.toAbsolutePath().normalize(), path -> {
            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                return parseAnchorCounts(content).keySet();
            } catch (IOException e) {
                return Set.of();
            }
        });
    }

    private Map<String, Integer> parseAnchorCounts(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        Map<String, Integer> anchors = new LinkedHashMap<>();
        String[] lines = body.replace("\r", "").split("\n");
        for (String line : lines) {
            String trimmed = line.stripLeading();
            if (!trimmed.startsWith("#")) {
                continue;
            }
            int i = 0;
            while (i < trimmed.length() && trimmed.charAt(i) == '#') {
                i++;
            }
            if (i > 0 && i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i))) {
                String heading = trimmed.substring(i).trim();
                if (!heading.isEmpty()) {
                    String anchor = heading.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9\\s-]", "")
                        .replaceAll("\\s+", "-")
                        .replaceAll("-+", "-")
                        .replaceAll("^-", "")
                        .replaceAll("-$", "");
                    if (!anchor.isEmpty()) {
                        anchors.merge(anchor, 1, Integer::sum);
                    }
                }
            }
        }
        return anchors;
    }

    private String extractAnchor(String url) {
        int hash = url.indexOf('#');
        if (hash < 0) {
            return null;
        }
        String fragment = url.substring(hash + 1);
        int query = fragment.indexOf('?');
        if (query >= 0) {
            fragment = fragment.substring(0, query);
        }
        return fragment.trim();
    }

    private void traverse(Node node, Consumer<String> linkConsumer) {
        if (node instanceof Link link) {
            linkConsumer.accept(link.getUrl().toString());
        } else if (node instanceof Image image) {
            linkConsumer.accept(image.getUrl().toString());
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            traverse(child, linkConsumer);
        }
    }

    private String sanitizeUrl(String url) {
        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("#")) {
            return null;
        }
        if (trimmed.contains("{{") || trimmed.contains("}}")) {
            return null;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("mailto:")) {
            return null;
        }
        int schemeIdx = trimmed.indexOf(':');
        if (schemeIdx > 0 && trimmed.indexOf('/') > schemeIdx) {
            return null;
        }
        int hash = trimmed.indexOf('#');
        if (hash >= 0) {
            trimmed = trimmed.substring(0, hash);
        }
        int query = trimmed.indexOf('?');
        if (query >= 0) {
            trimmed = trimmed.substring(0, query);
        }
        return trimmed.trim();
    }

    private Path resolveLinkTarget(String sanitized, Path file, Path repoRoot) {
        if (sanitized.isEmpty()) {
            return null;
        }
        Path base;
        if (sanitized.startsWith("/")) {
            base = repoRoot.resolve(sanitized.substring(1));
        } else {
            base = file.getParent().resolve(sanitized);
        }
        Path normalized = base.normalize();
        if (!normalized.startsWith(repoRoot)) {
            return null;
        }
        return normalized;
    }

    private boolean isUnderExcludedRoots(Path resolved, Path repoRoot) {
        for (Path excluded : EXCLUDED_LINK_ROOTS) {
            Path excludedRoot = repoRoot.resolve(excluded);
            if (resolved.startsWith(excludedRoot)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresFrontMatter(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        for (String prefix : FRONTMATTER_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private FrontMatter parseFrontMatter(String content) {
        String[] lines = content.split("\\R", -1);
        if (lines.length == 0 || !lines[0].trim().equals("---")) {
            return new FrontMatter(false, Map.of(), content);
        }
        StringBuilder yaml = new StringBuilder();
        boolean closed = false;
        int closingLine = -1;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().equals("---")) {
                closed = true;
                closingLine = i;
                break;
            }
            yaml.append(lines[i]).append('\n');
        }
        if (!closed) {
            throw new IllegalArgumentException("unterminated front matter");
        }
        Object loaded = YAML_LOADER.loadFromString(yaml.toString());
        Map<String, Object> values;
        if (loaded instanceof Map<?, ?> map) {
            values = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    values.put(entry.getKey().toString(), entry.getValue());
                }
            }
        } else {
            values = new LinkedHashMap<>();
        }
        int bodyStartLine = closingLine + 1;
        StringBuilder body = new StringBuilder();
        for (int i = bodyStartLine; i < lines.length; i++) {
            body.append(lines[i]);
            if (i < lines.length - 1) {
                body.append('\n');
            }
        }
        return new FrontMatter(true, values, body.toString());
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().trim().toLowerCase(Locale.ROOT);
    }

    private String deriveModule(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (!path.contains("/")) {
            return path;
        }
        return path.substring(0, path.indexOf('/'));
    }

    private Args parseArgs(String[] argv) {
        Path defaultInventory = resolveDefaultInventory();
        Path inventory = defaultInventory;
        Path output = Paths.get("target/doclint-report.json").toAbsolutePath().normalize();
        boolean help = false;

        List<String> args = new ArrayList<>(Arrays.asList(argv));
        for (int i = 0; i < args.size(); i++) {
            String word = args.get(i);
            switch (word) {
                case "-h":
                case "--help":
                    help = true;
                    break;
                case "--inventory":
                    inventory = resolveRequiredPath(args, ++i, "inventory");
                    break;
                case "-o":
                case "--output":
                    output = resolveRequiredPath(args, ++i, "output");
                    break;
                default:
                    throw new IllegalArgumentException("unknown argument: " + word);
            }
        }
        return new Args(inventory, output, help);
    }

    private Path resolveDefaultInventory() {
        List<Path> candidates = List.of(
            Paths.get("target/docs_inventory.json"),
            Paths.get("nb-docsys/target/docs_inventory.json"),
            Paths.get("docs/docs_inventory.json")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        return candidates.get(0).toAbsolutePath().normalize();
    }

    private Path resolveRequiredPath(List<String> args, int index, String name) {
        if (index >= args.size()) {
            throw new IllegalArgumentException("missing value for --" + name);
        }
        Path candidate = Paths.get(args.get(index));
        return candidate.isAbsolute() ? candidate : Paths.get("").toAbsolutePath().resolve(candidate).normalize();
    }

    private Path resolveRepoRoot(String recordedRoot) throws IOException {
        if (recordedRoot != null && !recordedRoot.isBlank()) {
            Path candidate = Paths.get(recordedRoot);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        // fallback: walk up until we find .git
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IOException("Unable to locate repository root for doclint.");
    }

    private void printUsage() {
        System.out.println(String.join(System.lineSeparator(),
            "Usage: nb app docs-lint [--inventory <path>] [--output <path>]",
            "  --inventory <path>  Inventory JSON produced by docs-inventory",
            "  -o, --output <path> JSON report output (default: target/doclint-report.json)",
            "  -h, --help          Show this help text"
        ));
    }


    private void writeJsonReport(Path output, LintResult result) throws IOException {
        ObjectNode report = MAPPER.createObjectNode()
            .put("errors", result.messages().size())
            .set("messages", MAPPER.valueToTree(result.messages()));
        ObjectNode byFile = MAPPER.createObjectNode();
        result.byFile().forEach((path, errors) ->
            byFile.set(path, MAPPER.valueToTree(errors))
        );
        report.set("by_file", byFile);
        report.set("link_graph", MAPPER.valueToTree(result.linkGraph()));
        report.set("orphans", MAPPER.valueToTree(result.orphans()));
        Path parent = output.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
    }

    private record Args(Path inventory, Path output, boolean help) {}

    private record FrontMatter(boolean present, Map<String, Object> values, String body) {}

    private record LintResult(List<String> messages, Map<String, List<String>> byFile, Map<String, Set<String>> linkGraph, Set<String> orphans) {}
}
