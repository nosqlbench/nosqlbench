package io.nosqlbench.docsys.apps.renderzola;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sigstore.ImmutableVerificationOptions;
import dev.sigstore.KeylessVerificationException;
import dev.sigstore.KeylessVerifier;
import dev.sigstore.VerificationOptions;
import dev.sigstore.bundle.Bundle;
import dev.sigstore.bundle.BundleParseException;
import dev.sigstore.fulcio.client.ImmutableFulcioCertificateMatcher;
import dev.sigstore.strings.RegexSyntaxException;
import dev.sigstore.strings.StringMatcher;
import dev.sigstore.trustroot.SigstoreConfigurationException;
import io.nosqlbench.docsys.apps.docinventory.DocInventoryApp;
import io.nosqlbench.docsys.apps.doclint.DocLintApp;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;


@Service(value = BundledApp.class, selector = "docs-render-zola")
public class RenderDocsZolaApp implements BundledApp {

    private static final String DEFAULT_TITLE = "NoSQLBench Documentation";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    private static final String USER_AGENT = "docs-render-zola/1.0 (+https://nosqlbench.io)";
    private static final String DEFAULT_SAN_REGEX = "https://github.com/.+";
    private static final String DEFAULT_ISSUER_REGEX = "https://token.actions.githubusercontent.com";
    private static final SigstoreBundleReader DEFAULT_BUNDLE_READER = new DefaultSigstoreBundleReader();
    private static final SigstoreVerifierFacade DEFAULT_SIGSTORE_VERIFIER = new LibrarySigstoreVerifier();
    private static volatile SigstoreBundleReader bundleReader = DEFAULT_BUNDLE_READER;
    private static volatile SigstoreVerifierFacade sigstoreVerifier = DEFAULT_SIGSTORE_VERIFIER;
    private static final String DEFAULT_THEME_REPO = "https://github.com/Jieiku/abridge.git";
    private static final String DEFAULT_THEME_REF = "main";

    @Override
    public int applyAsInt(String[] args) {
        CommandLine cmd = new CommandLine(new BuildCommand());
        cmd.addSubcommand("install", new InstallCommand());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        cmd.setUnmatchedArgumentsAllowed(false);
        cmd.setUsageHelpWidth(120);
        return cmd.execute(args);
    }

    public static void main(String[] args) {
        int exit = new RenderDocsZolaApp().applyAsInt(args);
        if (exit != 0) {
            System.exit(exit);
        }
    }

    private static void unzip(Path zip, Path target) throws IOException {
        Files.createDirectories(target);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = target.resolve(entry.getName()).normalize();
                if (!entryPath.startsWith(target)) {
                    throw new IOException("Zip entry attempts to escape target: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath);
                }
                zis.closeEntry();
            }
        }
    }

    private static void copyGeneratedDocs(Path exportRoot, Path generatedRoot, Set<String> curatedReferenceNames) throws IOException {
        Files.walk(exportRoot)
            .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".md"))
            .forEach(path -> {
                Path relative = exportRoot.relativize(path);
                String basename = relative.getFileName().toString().toLowerCase(Locale.ROOT);
                if (curatedReferenceNames.contains(basename)) {
                    throw new IllegalStateException("Duplicate reference doc detected in curated site: " + basename
                        + " (generated path " + relative + "). Remove the curated copy or rename one of them.");
                }
                Path target = generatedRoot.resolve(relative);
                try {
                    Files.createDirectories(target.getParent());
                    ensureSectionIndices(generatedRoot, relative.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {

                    throw new RuntimeException("Unable to copy generated doc " + path + " -> " + target, e);
                }
            });
    }

    private static void ensureSectionIndices(Path contentRoot, Path relativeDir) throws IOException {
        if (relativeDir == null) {
            return;
        }
        Path current = relativeDir;
        while (current != null && current.getNameCount() > 0) {
            Path dir = contentRoot.resolve(current);
            Path index = dir.resolve("_index.md");
            if (!Files.exists(index)) {
                Files.createDirectories(dir);
                Files.writeString(index,
                    "---\n" +
                        "title: \"" + current.getFileName().toString() + "\"\n" +
                        "description: \"Auto-generated section\"\n" +
                        "template: \"index.html\"\n" +
                        "paginate_by: 200\n" +
                        "---\n",
                    StandardCharsets.UTF_8);
            }
            current = current.getParent();
        }
    }

    private static void ensureRootIndex(Path index) throws IOException {
        if (Files.exists(index)) {
            return;
        }
        Files.createDirectories(index.getParent());
        String body = """
            ---
            title: "Documentation"
            description: "Generated by docs-render-zola"
            template: "pages.html"
            ---

            ## Welcome

            Start exploring the docs at [Introduction](/introduction/).
            """;
        Files.writeString(index, body, StandardCharsets.UTF_8);
    }

    private static void writeGeneratedIndex(Path generatedRoot) throws IOException {
        Files.createDirectories(generatedRoot);
        Path index = generatedRoot.resolve("_index.md");
        List<String> links = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(generatedRoot)) {
            stream.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                .filter(p -> !p.getFileName().toString().equalsIgnoreCase("_index.md"))
                .sorted()
                .forEach(path -> {
                    Path rel = generatedRoot.relativize(path);
                    String withoutExt = rel.toString().replaceAll("\\\\", "/").replaceAll("\\.md$", "");
                    String display = rel.getFileName().toString().replace(".md", "");
                    links.add("- [" + display + "](./" + withoutExt + "/)");
                });
        }
        StringBuilder body = new StringBuilder();
        body.append("---\n")
            .append("title: \"Reference (generated)\"\n")
            .append("description: \"Auto-generated reference docs\"\n")
            .append("template: \"pages.html\"\n")
            .append("---\n\n")
            .append("## Generated Reference\n");
        links.forEach(link -> body.append(link).append("\n"));
        Files.writeString(index, body.toString(), StandardCharsets.UTF_8);
    }

    private static void writeConfig(Path projectRoot) throws IOException {
        String config = ""
            + "base_url = \"/\"\n"
            + "title = \"" + DEFAULT_TITLE + "\"\n"
            + "theme = \"abridge\"\n"
            + "compile_sass = false\n"
            + "build_search_index = true\n"
            + "\n[markdown]\n"
            + "highlight_code = true\n"
            + "\n[extra]\n"
            + "docs_base = \"\"\n"
            + "menu = [ {url = \"introduction\", name = \"Docs\", slash = true, blank = false, size=\"s110\"}, {url = \"reference-generated\", name = \"Reference (Generated)\", slash = true, blank = false, size=\"s110\"} ]\n";
        Files.writeString(projectRoot.resolve("config.toml"), config, StandardCharsets.UTF_8);
    }

    private static void runZola(String zolaBin, Path projectRoot) throws IOException, InterruptedException {
        List<String> command = List.of(zolaBin, "build");
        logCommand(command, projectRoot);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectRoot.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String log;
        try (InputStream in = process.getInputStream()) {
            log = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IllegalStateException("zola build failed:\n" + log);
        }
    }

    private static void zipDirectory(Path source, Path targetZip) throws IOException {
        Files.createDirectories(targetZip.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetZip))) {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    writeEntry(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(source)) {
                        writeEntry(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                private void writeEntry(Path path) throws IOException {
                    Path relative = source.relativize(path);
                    String entryName = relative.toString().replace('\\', '/');
                    ZipEntry entry = new ZipEntry(path.equals(source) ? "" : entryName + (Files.isDirectory(path) ? "/" : ""));
                    if (entry.getName().isEmpty()) {
                        return;
                    }
                    zos.putNextEntry(entry);
                    if (Files.isRegularFile(path)) {
                        Files.copy(path, zos);
                    }
                    zos.closeEntry();
                }
            });
        }
    }

    private static Path defaultCacheRoot() {
        String home = System.getProperty("user.home", ".");
        return Path.of(home, ".cache", "nosqlbench", "zola");
    }

    private static Path defaultThemeCacheRoot() {
        String home = System.getProperty("user.home", ".");
        return Path.of(home, ".cache", "nosqlbench", "zola-themes");
    }

    private static String defaultLinkName() {
        return isWindows() ? "zola.exe" : "zola";
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win");
    }

    private static void prepareTheme(Path themeTarget, Path localThemeDir, Path themeCache,
                                     String repo, String ref) throws IOException {
        Path source;
        if (localThemeDir != null) {
            source = localThemeDir.toAbsolutePath().normalize();
            if (!Files.isDirectory(source)) {
                throw new IOException("Theme directory does not exist: " + source);
            }
        } else {
            Path cache = themeCache != null ? themeCache.toAbsolutePath().normalize() : defaultThemeCacheRoot();
            Files.createDirectories(cache);
            source = cloneTheme(cache, repo, ref);
        }
        copyDirectory(source, themeTarget);
    }

    /**
     * Abridge does not ship a section.html, so Zola will render sections with a fallback page
     * unless we provide one. Use the theme's pages.html for sections so section landing pages
     * render with the normal site chrome.
     */
    private static void ensureSectionTemplate(Path projectRoot) throws IOException {
        Path templates = projectRoot.resolve("templates");
        Files.createDirectories(templates);
        Path sectionTemplate = templates.resolve("section.html");
        if (!Files.exists(sectionTemplate)) {
            Files.writeString(sectionTemplate, "{% extends \"pages.html\" %}\n", StandardCharsets.UTF_8);
        }
    }

    private static Path cloneTheme(Path cache, String repo, String ref) throws IOException {
        String folder = sanitizeRepoName(repo);
        List<String> candidates = new ArrayList<>();
        if (ref != null && !ref.isBlank()) {
            candidates.add(ref);
            if ("main".equalsIgnoreCase(ref)) {
                candidates.add("master");
            } else if ("master".equalsIgnoreCase(ref)) {
                candidates.add("main");
            }
        }
        // Always allow falling back to the repo default branch if specific refs fail.
        candidates.add("");

        IOException lastError = null;
        for (String candidateRef : candidates) {
            Path cloneDir = cache.resolve(folder + (candidateRef.isBlank() ? "" : "-" + candidateRef.replace('/', '_')));
            deleteRecursively(cloneDir);
            Files.createDirectories(cache);
            ProcessBuilder pb;
            if (!candidateRef.isBlank()) {
                pb = new ProcessBuilder("git", "clone", "--depth", "1", "--branch", candidateRef, repo, cloneDir.toString());
            } else {
                pb = new ProcessBuilder("git", "clone", "--depth", "1", repo, cloneDir.toString());
            }
            logCommand(pb.command(), cache);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String log;
            try (InputStream in = process.getInputStream()) {
                log = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            try {
                int exit = process.waitFor();
                if (exit == 0) {
                    return cloneDir;
                }
                lastError = new IOException("git clone failed (" + exit + ")"
                    + (candidateRef.isBlank() ? " (default branch)" : " for ref '" + candidateRef + "'")
                    + ": " + log);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while cloning theme", e);
            }
        }
        throw (lastError != null) ? lastError : new IOException("git clone failed with no diagnostics");
    }

    private static String sanitizeRepoName(String repo) {
        if (repo == null || repo.isBlank()) {
            return "theme";
        }
        String[] parts = repo.split("/");
        String name = parts[parts.length - 1];
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - 4);
        }
        return name.isBlank() ? "theme" : name;
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void copyCuratedSite(Path curatedRoot, Path contentRoot) throws IOException {
        Files.walk(curatedRoot)
            .forEach(source -> {
                try {
                    Path target = contentRoot.resolve(curatedRoot.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to copy curated site content: " + source, e);
                }
            });
    }

    private static Set<String> listBasenames(Path refRoot) throws IOException {
        Set<String> names = new HashSet<>();
        if (!Files.isDirectory(refRoot)) {
            return names;
        }
        try (Stream<Path> stream = Files.walk(refRoot)) {
            stream.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                .forEach(p -> names.add(p.getFileName().toString().toLowerCase(Locale.ROOT)));
        }
        return names;
    }

    @Command(name = "docs-render-zola", mixinStandardHelpOptions = true,
        description = "Render the docs-export bundle into a Zola site (abridge theme).")
    static class BuildCommand implements Runnable {

        @Option(names = "--export", description = "exported_docs.zip produced by nb5 docs-export",
            defaultValue = "exported_docs.zip")
        Path exportZip;

        @Option(names = "--output", description = "Zipfile containing the resulting Zola site/project",
            defaultValue = "rendered-docs-zola.zip")
        Path outputZip;

        @Option(names = "--zola-bin", description = "Path to the zola executable",
            defaultValue = "zola")
        String zolaBin;

        @Option(names = "--skip-build", description = "Emit the Zola project without running `zola build`")
        boolean skipBuild;

        @Option(names = {"-f", "--force"}, description = "Overwrite existing output zip if present")
        boolean force;

        @Option(names = "--theme-dir", description = "Path to an existing abridge theme to copy instead of cloning.")
        Path themeDir;

        @Option(names = "--theme-cache", description = "Cache directory for cloned themes.")
        Path themeCache;

        @Option(names = "--theme-git", description = "Git repository for the abridge theme.",
            defaultValue = DEFAULT_THEME_REPO)
        String themeRepo;

        @Option(names = "--theme-ref", description = "Branch or tag to checkout from the theme repository.",
            defaultValue = DEFAULT_THEME_REF)
        String themeRef;

        @Override
        public void run() {
            try {
                Path repoRoot = locateRepoRoot();
                Path normalizedExport = exportZip.toAbsolutePath().normalize();
                Path normalizedOutput = outputZip.toAbsolutePath().normalize();
                boolean regenerateExport = !Files.exists(normalizedExport) || force;
                if (regenerateExport) {
                    System.out.printf("docs-render-zola: %s export bundle; generating via docs-inventory, docs-lint, docs-export%n",
                        Files.exists(normalizedExport) ? "refreshing" : "creating");
                    generateExportBundle(normalizedExport, force);
                }
                if (Files.exists(normalizedOutput)) {
                    if (!force) {
                        throw new IllegalStateException("Output file already exists: " + normalizedOutput + " (use -f to overwrite)");
                    }
                    Files.delete(normalizedOutput);
                }
                Path workspace = Files.createTempDirectory("docs-render-zola");
                try {
                    Path exportDir = workspace.resolve("export");
                    unzip(normalizedExport, exportDir);

                    Path projectRoot = workspace.resolve("zola-site");
                    Files.createDirectories(projectRoot);

                    writeConfig(projectRoot);
                    prepareTheme(projectRoot.resolve("themes/abridge"),
                        themeDir, themeCache, themeRepo, themeRef);
                    ensureSectionTemplate(projectRoot);
                    Path contentRoot = projectRoot.resolve("content");
                    Files.createDirectories(contentRoot);

                    Path curatedRoot = repoRoot.resolve("docs/site");
                    if (Files.isDirectory(curatedRoot)) {
                        copyCuratedSite(curatedRoot, contentRoot);
                    }

                    Set<String> curatedReferenceNames = listBasenames(contentRoot.resolve("reference"));
                    Path generatedRoot = contentRoot.resolve("reference-generated");
                    Files.createDirectories(generatedRoot);
                    copyGeneratedDocs(exportDir, generatedRoot, curatedReferenceNames);
                    writeGeneratedIndex(generatedRoot);

                    ensureRootIndex(projectRoot.resolve("content/_index.md"));

                    Path publicDir = projectRoot.resolve("public");
                    if (!skipBuild) {
                        runZola(zolaBin, projectRoot);
                        if (!Files.exists(publicDir)) {
                            throw new IllegalStateException("zola build completed but public directory was not created");
                        }
                        zipDirectory(publicDir, normalizedOutput);
                    } else {
                        zipDirectory(projectRoot, normalizedOutput);
                    }

                    System.out.printf("docs-render-zola: wrote %s (skip-build=%s)%n",
                        normalizedOutput, skipBuild);
                } finally {
                    deleteRecursively(workspace);
                }
            } catch (IOException | InterruptedException e) {
                throw new CommandLine.ExecutionException(new CommandLine(this),
                    "docs-render-zola failed: " + e.getMessage(), e);
            }
        }
    }

    private static void generateExportBundle(Path exportZip, boolean force) throws IOException {
        Path repoRoot = locateRepoRoot();
        Path inventory = repoRoot.resolve("nb-docsys/target/docs_inventory.json");
        Path doclintReport = repoRoot.resolve("nb-docsys/target/doclint-report.json");
        if (force) {
            Files.deleteIfExists(exportZip);
            Files.deleteIfExists(inventory);
            Files.deleteIfExists(doclintReport);
        }
        Files.createDirectories(inventory.getParent());
        Files.createDirectories(doclintReport.getParent());

        runBundledApp(new DocInventoryApp(), new String[]{
            "--root", repoRoot.toString(),
            "--output", inventory.toString()
        }, "docs-inventory");

        runBundledApp(new DocLintApp(), new String[]{
            "--inventory", inventory.toString(),
            "-o", doclintReport.toString()
        }, "docs-lint");

        invokeDocsExport(exportZip, inventory, doclintReport);
    }

    private static void runBundledApp(BundledApp app, String[] args, String label) {
        int exit = app.applyAsInt(args);
        if (exit != 0) {
            throw new IllegalStateException(label + " failed with exit code " + exit);
        }
    }

    private static void invokeDocsExport(Path exportZip, Path inventory, Path doclintReport) {
        try {
            Class<?> exporterClass = Class.forName("io.nosqlbench.api.docsapi.docexporter.BundledMarkdownExporter");
            Object exporter = exporterClass.getDeclaredConstructor().newInstance();
            var method = exporterClass.getMethod("applyAsInt", String[].class);
            String[] args = new String[]{
                "--zipfile", exportZip.toString(),
                "--inventory", inventory.toString(),
                "--doclint-report", doclintReport.toString(),
                "--force"
            };
            int exit = (Integer) method.invoke(exporter, (Object) args);
            if (exit != 0) {
                throw new IllegalStateException("docs-export failed with exit code " + exit);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("docs-export app not available on the classpath; cannot build exported_docs.zip", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke docs-export via reflection", e);
        }
    }

    private static Path locateRepoRoot() throws IOException {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IOException("Unable to locate repository root for docs-render-zola.");
    }


    private static VerificationOptions buildVerificationOptions(String issuerRegex, String sanRegex)
        throws RegexSyntaxException {
        String issuer = normalizeRegex(issuerRegex);
        String san = normalizeRegex(sanRegex);
        if (issuer == null && san == null) {
            return VerificationOptions.empty();
        }
        ImmutableFulcioCertificateMatcher.Builder matcher = ImmutableFulcioCertificateMatcher.builder();
        if (issuer != null) {
            matcher.issuer(StringMatcher.regex(issuer));
        }
        if (san != null) {
            matcher.subjectAlternativeName(StringMatcher.regex(san));
        }
        return ImmutableVerificationOptions.builder()
            .addCertificateMatchers(matcher.build())
            .build();
    }

    private static String normalizeRegex(String candidate) {
        if (candidate == null) {
            return null;
        }
        String trimmed = candidate.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static void setSigstoreBundleReaderForTesting(SigstoreBundleReader reader) {
        bundleReader = reader == null ? DEFAULT_BUNDLE_READER : reader;
    }

    static void setSigstoreVerifierForTesting(SigstoreVerifierFacade verifier) {
        sigstoreVerifier = verifier == null ? DEFAULT_SIGSTORE_VERIFIER : verifier;
    }

    static void resetSigstoreTestingHooks() {
        bundleReader = DEFAULT_BUNDLE_READER;
        sigstoreVerifier = DEFAULT_SIGSTORE_VERIFIER;
    }

    interface SigstoreBundleReader {
        Bundle read(Path bundlePath) throws IOException, BundleParseException;
    }

    interface SigstoreVerifierFacade {
        void verify(Path artifact, Bundle bundle, VerificationOptions options) throws Exception;
    }

    @Command(name = "install",
        description = "Download, verify, and cache the Zola binary so docs-render-zola can build sites without extra tooling.")
    static class InstallCommand implements Runnable {

        @Option(names = "--repo", description = "GitHub repo to pull releases from.",
            defaultValue = "getzola/zola")
        String repo;

        @Option(names = "--platform", description = "Release asset suffix to download.",
            defaultValue = "x86_64-unknown-linux-gnu")
        String platform;

        @Option(names = "--tag", description = "Release tag to install (default: latest).",
            defaultValue = "latest")
        String tag;

        @Option(names = "--cache-root", description = "Cache directory for installed Zola releases.")
        Path cacheRoot;

        @Option(names = "--link-name", description = "Shim or symlink name created under the cache root.")
        String linkName = "";

        @Option(names = "--token-env", description = "Environment variable that holds a GitHub API token.",
            defaultValue = "GITHUB_TOKEN")
        String tokenEnv = "GITHUB_TOKEN";

        @Option(names = "--force", description = "Re-download assets even if cached locally.")
        boolean force;

        @Option(names = "--api-base", description = "Override the GitHub API base (for testing).", hidden = true,
            defaultValue = "https://api.github.com")
        String apiBase = "https://api.github.com";

        @Option(names = "--release-file", description = "Use a local release metadata JSON file (testing only).", hidden = true)
        Path releaseMetadataFile;

        @Option(names = "--verify", description = "Verify an artifact + Sigstore bundle instead of installing.")
        boolean verify;

        @Option(names = "--artifact", description = "Artifact tarball to verify when --verify is provided.")
        Path artifactPath;

        @Option(names = "--bundle", description = "Sigstore bundle (.sigstore) to verify when --verify is provided.")
        Path bundlePath;

        @Option(names = "--expected-san", description = "Regex that the Fulcio certificate SAN must match.")
        String expectedSanRegex;

        @Option(names = "--expected-issuer", description = "Regex that the Fulcio certificate issuer must match.")
        String expectedIssuerRegex;

        @Override
        public void run() {
            if (verify) {
                runVerification();
                return;
            }
            runInstall();
        }

        private void runInstall() {
            CommandLine cmd = new CommandLine(this);
            Path cache = resolveCacheRoot();
            try {
                Files.createDirectories(cache);
                String token = resolveToken();
                ReleaseMetadata metadata = fetchReleaseMetadata(token);
                Path archive = cache.resolve(metadata.assetName());
                Path bundle = cache.resolve(metadata.bundleName());
                if (force || !Files.exists(archive)) {
                    downloadAsset(metadata.assetUrl(), archive, token);
                } else {
                    System.out.printf("docs-render-zola install: using cached archive %s%n", archive);
                }
                if (force || !Files.exists(bundle)) {
                    downloadAsset(metadata.bundleUrl(), bundle, token);
                } else {
                    System.out.printf("docs-render-zola install: using cached bundle %s%n", bundle);
                }
                String issuerPattern = coalesceRegex(expectedIssuerRegex, DEFAULT_ISSUER_REGEX);
                String sanPattern = coalesceRegex(expectedSanRegex, defaultSanForRepo());
                Bundle parsed = bundleReader.read(bundle);
                VerificationOptions options = buildVerificationOptions(issuerPattern, sanPattern);
                sigstoreVerifier.verify(archive, parsed, options);
                Path versionDir = installArchive(cache, metadata.tagName(), archive);
                Path binary = locateBinary(versionDir);
                ensureExecutable(binary);
                Path shim = cache.resolve(resolveLinkName());
                Files.createDirectories(shim.getParent() == null ? cache : shim.getParent());
                linkBinary(binary, shim);
                System.out.printf("docs-render-zola install: %s ready (%s -> %s)%n",
                    metadata.tagName(), shim, binary);
            } catch (BundleParseException e) {
                throw new CommandLine.ExecutionException(cmd, "Failed to parse Sigstore bundle: " + e.getMessage(), e);
            } catch (RegexSyntaxException e) {
                throw new CommandLine.ParameterException(cmd, "Invalid regex for certificate matching: " + e.getMessage());
            } catch (KeylessVerificationException e) {
                throw new CommandLine.ExecutionException(cmd, "Sigstore verification failed: " + e.getMessage(), e);
            } catch (SigstoreConfigurationException | CertificateException | InvalidKeySpecException
                     | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                throw new CommandLine.ExecutionException(cmd, "Unable to initialize Sigstore verifier: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CommandLine.ExecutionException(cmd, "Interrupted while installing zola", e);
            } catch (IOException e) {
                throw new CommandLine.ExecutionException(cmd, "I/O error while installing zola: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new CommandLine.ExecutionException(cmd, "Unexpected error while installing zola: " + e.getMessage(), e);
            }
        }

        private void runVerification() {
            CommandLine cmd = new CommandLine(this);
            Path artifact = requireExisting(cmd, artifactPath, "--artifact");
            Path bundle = requireExisting(cmd, bundlePath, "--bundle");
            String issuerPattern = coalesceRegex(expectedIssuerRegex, DEFAULT_ISSUER_REGEX);
            String sanPattern = coalesceRegex(expectedSanRegex, DEFAULT_SAN_REGEX);
            try {
                Bundle parsed = bundleReader.read(bundle);
                VerificationOptions options = buildVerificationOptions(issuerPattern, sanPattern);
                sigstoreVerifier.verify(artifact, parsed, options);
                System.out.printf("docs-render-zola install: verified %s using %s%n", artifact, bundle);
            } catch (BundleParseException e) {
                throw new CommandLine.ExecutionException(cmd, "Failed to parse Sigstore bundle: " + e.getMessage(), e);
            } catch (RegexSyntaxException e) {
                throw new CommandLine.ParameterException(cmd, "Invalid regex for certificate matching: " + e.getMessage());
            } catch (KeylessVerificationException e) {
                throw new CommandLine.ExecutionException(cmd, "Sigstore verification failed: " + e.getMessage(), e);
            } catch (SigstoreConfigurationException | CertificateException | InvalidKeySpecException
                     | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
                throw new CommandLine.ExecutionException(cmd, "Unable to initialize Sigstore verifier: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new CommandLine.ExecutionException(cmd, "I/O error while reading bundle: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new CommandLine.ExecutionException(cmd, "Unexpected error during verification: " + e.getMessage(), e);
            }
        }

        private ReleaseMetadata fetchReleaseMetadata(String token) throws IOException, InterruptedException {
            if (releaseMetadataFile != null) {
                byte[] bytes = Files.readAllBytes(releaseMetadataFile);
                JsonNode root = MAPPER.readTree(bytes);
                return parseReleaseMetadata(root);
            }
            String base = trimTrailingSlash(apiBase);
            String releasePath = "latest".equalsIgnoreCase(tag)
                ? "/repos/" + repo + "/releases/latest"
                : "/repos/" + repo + "/releases/tags/" + tag;
            URI uri = URI.create(base + releasePath);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", USER_AGENT);
            if (!token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = HTTP_CLIENT.send(builder.GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                throw new IOException("GitHub API request failed: " + response.statusCode());
            }
            JsonNode root = MAPPER.readTree(response.body());
            return parseReleaseMetadata(root);
        }

        private ReleaseMetadata parseReleaseMetadata(JsonNode root) throws IOException {
            String tagName = root.path("tag_name").asText();
            if (tagName == null || tagName.isBlank()) {
                throw new IOException("Release response missing tag_name");
            }
            String assetStem = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            String assetName = "zola-" + assetStem + "-" + platform + ".tar.gz";
            String bundleName = assetName + ".sigstore";
            JsonNode assets = root.path("assets");
            URI assetUrl = requireAssetUrl(assets, assetName);
            URI bundleUrl = requireAssetUrl(assets, bundleName);
            return new ReleaseMetadata(tagName, assetName, bundleName, assetUrl, bundleUrl);
        }

        private URI requireAssetUrl(JsonNode assets, String name) throws IOException {
            if (assets != null && assets.isArray()) {
                for (JsonNode asset : assets) {
                    if (name.equals(asset.path("name").asText())) {
                        String download = asset.path("browser_download_url").asText();
                        if (download == null || download.isBlank()) {
                            break;
                        }
                        return URI.create(download);
                    }
                }
            }
            throw new IOException("Release asset not found: " + name);
        }

        private void downloadAsset(URI url, Path target, String token) throws IOException, InterruptedException {
            Files.createDirectories(target.getParent());
            if ("file".equalsIgnoreCase(url.getScheme())) {
                Files.copy(Path.of(url), target, StandardCopyOption.REPLACE_EXISTING);
                return;
            }
            Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".partial");
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(url)
                    .timeout(Duration.ofMinutes(2))
                    .header("Accept", "*/*")
                    .header("User-Agent", USER_AGENT);
                if (!token.isBlank()) {
                    builder.header("Authorization", "Bearer " + token);
                }
                HttpResponse<Path> response = HTTP_CLIENT.send(builder.GET().build(),
                    HttpResponse.BodyHandlers.ofFile(temp));
                if (response.statusCode() / 100 != 2) {
                    throw new IOException("Download failed (" + response.statusCode() + "): " + url);
                }
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        }

        private Path installArchive(Path cache, String tagName, Path archive) throws IOException {
            Path tempExtract = Files.createTempDirectory("docs-render-zola-extract");
            boolean moved = false;
            try {
                extractTarGz(archive, tempExtract);
                Path versionDir = cache.resolve("zola-" + tagName);
                deleteRecursively(versionDir);
                Files.createDirectories(versionDir.getParent());
                Files.move(tempExtract, versionDir, StandardCopyOption.REPLACE_EXISTING);
                moved = true;
                return versionDir;
            } finally {
                if (!moved) {
                    deleteRecursively(tempExtract);
                }
            }
        }

        private Path locateBinary(Path versionDir) throws IOException {
            try (Stream<Path> stream = Files.walk(versionDir)) {
                return stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.equals("zola") || name.equals("zola.exe");
                    })
                    .findFirst()
                    .orElseThrow(() -> new IOException("zola binary not found in " + versionDir));
            }
        }

        private void ensureExecutable(Path binary) {
            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(binary);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                Files.setPosixFilePermissions(binary, perms);
            } catch (UnsupportedOperationException | IOException ignored) {
            }
        }

        private void linkBinary(Path binary, Path shim) throws IOException {
            Files.deleteIfExists(shim);
            try {
                Files.createSymbolicLink(shim, binary);
            } catch (UnsupportedOperationException | IOException | SecurityException e) {
                Files.copy(binary, shim, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        private Path resolveCacheRoot() {
            if (cacheRoot != null) {
                return cacheRoot.toAbsolutePath().normalize();
            }
            return defaultCacheRoot();
        }

        private String resolveLinkName() {
            if (linkName != null && !linkName.isBlank()) {
                return linkName;
            }
            return defaultLinkName();
        }

        private String resolveToken() {
            if (tokenEnv == null || tokenEnv.isBlank()) {
                return "";
            }
            String token = System.getenv(tokenEnv);
            return token == null ? "" : token.trim();
        }

        private String defaultSanForRepo() {
            if (repo == null || repo.isBlank()) {
                return DEFAULT_SAN_REGEX;
            }
            return "https://github.com/" + repo + "/.github/workflows/.+@refs/.*";
        }

        private Path requireExisting(CommandLine cmd, Path input, String optionName) {
            if (input == null) {
                throw new CommandLine.ParameterException(cmd, optionName + " is required when --verify is set");
            }
            Path normalized = input.toAbsolutePath().normalize();
            if (!Files.exists(normalized)) {
                throw new CommandLine.ParameterException(cmd, "Path not found for " + optionName + ": " + normalized);
            }
            return normalized;
        }
    }

    private static String coalesceRegex(String candidate, String fallback) {
        if (candidate == null || candidate.isBlank()) {
            return fallback;
        }
        return candidate;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static void extractTarGz(Path archive, Path destination) throws IOException {
        Files.createDirectories(destination);
        byte[] buffer = new byte[8192];
        try (InputStream in = new BufferedInputStream(Files.newInputStream(archive));
             GzipCompressorInputStream gzip = new GzipCompressorInputStream(in);
             TarArchiveInputStream tar = new TarArchiveInputStream(gzip)) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                Path resolved = destination.resolve(entry.getName()).normalize();
                if (!resolved.startsWith(destination)) {
                    throw new IOException("Tar entry escapes destination: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else if (entry.isSymbolicLink()) {
                    Path linkTarget = Path.of(entry.getLinkName());
                    try {
                        Files.createSymbolicLink(resolved, linkTarget);
                    } catch (UnsupportedOperationException | IOException ignored) {
                    }
                } else {
                    Files.createDirectories(resolved.getParent());
                    try (OutputStream out = Files.newOutputStream(resolved)) {
                        int len;
                        while ((len = tar.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source) || !Files.isDirectory(source)) {
            throw new IOException("Theme directory not found: " + source);
        }
        deleteRecursively(target);
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                if (relative.toString().contains(".git")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Path copyTarget = target.resolve(relative);
                Files.createDirectories(copyTarget);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                if (relative.toString().contains(".git")) {
                    return FileVisitResult.CONTINUE;
                }
                Path copyTarget = target.resolve(relative);
                Files.createDirectories(copyTarget.getParent());
                Files.copy(file, copyTarget, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void logCommand(List<String> command, Path workdir) {
        String cmd = String.join(" ", command);
        String cwd = workdir == null ? "N/A" : workdir.toAbsolutePath().normalize().toString();
        System.err.printf("docs-render-zola: exec [%s] (cwd=%s)%n", cmd, cwd);
    }

    private record ReleaseMetadata(String tagName, String assetName, String bundleName, URI assetUrl, URI bundleUrl) {
    }

    private static final class DefaultSigstoreBundleReader implements SigstoreBundleReader {
        @Override
        public Bundle read(Path bundlePath) throws IOException, BundleParseException {
            return Bundle.from(bundlePath, StandardCharsets.UTF_8);
        }
    }

    private static final class LibrarySigstoreVerifier implements SigstoreVerifierFacade {
        @Override
        public void verify(Path artifact, Bundle bundle, VerificationOptions options) throws Exception {
            KeylessVerifier verifier = KeylessVerifier.builder()
                .sigstorePublicDefaults()
                .build();
            verifier.verify(artifact, bundle, options == null ? VerificationOptions.empty() : options);
        }
    }

}
