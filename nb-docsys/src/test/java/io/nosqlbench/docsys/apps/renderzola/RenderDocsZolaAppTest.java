package io.nosqlbench.docsys.apps.renderzola;

import dev.sigstore.VerificationOptions;
import dev.sigstore.bundle.Bundle;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertPath;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RenderDocsZolaAppTest {

    @AfterEach
    void resetSigstoreHooks() {
        RenderDocsZolaApp.resetSigstoreTestingHooks();
    }

    @Test
    void createsZolaProjectWhenSkipBuild() throws IOException {
        Path tempDir = Files.createTempDirectory("render-docs-zola-test");
        Path themeDir = createThemeDirectory();
        try {
            Path exportZip = tempDir.resolve("exported_docs.zip");
            createExportZip(exportZip);
            Path outputZip = tempDir.resolve("rendered-docs-zola.zip");

            RenderDocsZolaApp app = new RenderDocsZolaApp();
            int exit = app.applyAsInt(new String[] {
                "--export", exportZip.toString(),
                "--output", outputZip.toString(),
                "--skip-build",
                "--theme-dir", themeDir.toString()
            });
            assertThat(exit).isEqualTo(0);
            assertThat(outputZip).exists();
            assertThat(zipContains(outputZip, "config.toml")).isTrue();
            assertThat(zipContains(outputZip, "content/docs/docs/example.md")).isTrue();
            assertThat(zipContains(outputZip, "themes/abridge/templates/base.html")).isTrue();
        } finally {
            deleteRecursively(tempDir);
            deleteRecursively(themeDir);
        }
    }

    @Test
    void installCommandDownloadsAndCachesZola() throws Exception {
        String assetName = "zola-9.9.9-x86_64-unknown-linux-gnu.tar.gz";
        byte[] archiveBytes = createTarGzBytes("zola", "binary".getBytes(StandardCharsets.UTF_8));
        byte[] bundleBytes = "{}".getBytes(StandardCharsets.UTF_8);

        Path tempDir = Files.createTempDirectory("render-docs-zola-release");
        Path assetFile = tempDir.resolve(assetName);
        Path bundleFile = tempDir.resolve(assetName + ".sigstore");
        Files.write(assetFile, archiveBytes);
        Files.write(bundleFile, bundleBytes);

        Path releaseJson = tempDir.resolve("release.json");
        String json = """
            {
              "tag_name": "v9.9.9",
              "assets": [
                {"name": "%s", "browser_download_url": "%s"},
                {"name": "%s.sigstore", "browser_download_url": "%s"}
              ]
            }
            """.formatted(assetName, assetFile.toUri(), assetName, bundleFile.toUri());
        Files.writeString(releaseJson, json);

        Path cacheRoot = Files.createTempDirectory("render-docs-zola-install");
        try {
            RecordingVerifier verifier = new RecordingVerifier();
            RenderDocsZolaApp.setSigstoreBundleReaderForTesting(path -> new DummyBundle());
            RenderDocsZolaApp.setSigstoreVerifierForTesting(verifier);

            RenderDocsZolaApp app = new RenderDocsZolaApp();
            int exit = app.applyAsInt(new String[] {
                "install",
                "--cache-root", cacheRoot.toString(),
                "--release-file", releaseJson.toString(),
                "--expected-san", "",
                "--expected-issuer", ""
            });
            assertThat(exit).isEqualTo(0);
            assertThat(verifier.invocations).isEqualTo(1);
            assertThat(cacheRoot.resolve("zola")).exists();
            assertThat(cacheRoot.resolve("zola-v9.9.9")).exists();
        } finally {
            deleteRecursively(cacheRoot);
            deleteRecursively(tempDir);
        }
    }

    @Test
    void verifyModeInvokesSigstoreFacade() throws IOException {
        Path tempDir = Files.createTempDirectory("render-docs-zola-verify");
        try {
            Path artifact = tempDir.resolve("zola.tar.gz");
            Files.writeString(artifact, "dummy");
            Path bundle = tempDir.resolve("zola.sigstore");
            Files.writeString(bundle, "{}");

            RecordingVerifier verifier = new RecordingVerifier();
            RenderDocsZolaApp.setSigstoreBundleReaderForTesting(path -> new DummyBundle());
            RenderDocsZolaApp.setSigstoreVerifierForTesting(verifier);

            RenderDocsZolaApp app = new RenderDocsZolaApp();
            int exit = app.applyAsInt(new String[] {
                "install",
                "--verify",
                "--artifact", artifact.toString(),
                "--bundle", bundle.toString(),
                "--expected-san", "",
                "--expected-issuer", ""
            });
            assertThat(exit).isEqualTo(0);
            assertThat(verifier.invocations).isEqualTo(1);
            assertThat(verifier.lastArtifact).isEqualTo(artifact.toAbsolutePath().normalize());
            assertThat(verifier.lastOptions).isNotNull();
            assertThat(verifier.lastOptions.getCertificateMatchers()).isNotEmpty();
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void createExportZip(Path exportZip) throws IOException {
        Files.createDirectories(exportZip.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(exportZip))) {
            ZipEntry entry = new ZipEntry("docs/example.md");
            zos.putNextEntry(entry);
            String markdown = "---\n" +
                "title: \"Sample\"\n" +
                "audience: user\n" +
                "diataxis: reference\n" +
                "component: docsys\n" +
                "topic: docops\n" +
                "status: live\n" +
                "tags: [docs]\n" +
                "---\n\n" +
                "# Sample\n\nHello world.\n";
            zos.write(markdown.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }

    private boolean zipContains(Path zip, String expected) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(expected)) {
                    return true;
                }
                zis.closeEntry();
            }
        }
        return false;
    }

    private byte[] createTarGzBytes(String fileName, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(baos);
             TarArchiveOutputStream tar = new TarArchiveOutputStream(gzip)) {
            TarArchiveEntry entry = new TarArchiveEntry(fileName);
            entry.setSize(content.length);
            tar.putArchiveEntry(entry);
            tar.write(content);
            tar.closeArchiveEntry();
        }
        return baos.toByteArray();
    }

    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walk(root)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
    }

    private Path createThemeDirectory() throws IOException {
        Path dir = Files.createTempDirectory("abridge-theme");
        Path templates = dir.resolve("templates");
        Path staticDir = dir.resolve("static/css");
        Files.createDirectories(templates);
        Files.createDirectories(staticDir);
        Files.writeString(templates.resolve("base.html"), "<html><body>{% block content %}{% endblock content %}</body></html>");
        Files.writeString(templates.resolve("page.html"), "{% extends \"base.html\" %}{% block content %}<article>{{ page.content | safe }}</article>{% endblock content %}");
        Files.writeString(templates.resolve("section.html"), "{% extends \"base.html\" %}{% block content %}<h1>{{ section.title }}</h1>{% endblock content %}");
        Files.writeString(templates.resolve("index.html"), "{% extends \"base.html\" %}{% block content %}<h1>{{ section.title }}</h1>{% endblock content %}");
        Files.writeString(staticDir.resolve("site.css"), "body{font-family:sans-serif;}");
        return dir;
    }

    private static final class DummyBundle extends Bundle {
        @Override
        public Optional<MessageSignature> getMessageSignature() {
            return Optional.empty();
        }

        @Override
        public Optional<DsseEnvelope> getDsseEnvelope() {
            return Optional.empty();
        }

        @Override
        public CertPath getCertPath() {
            return null;
        }

        @Override
        public List<dev.sigstore.rekor.client.RekorEntry> getEntries() {
            return List.of();
        }

        @Override
        public List<Timestamp> getTimestamps() {
            return List.of();
        }
    }

    private static final class RecordingVerifier implements RenderDocsZolaApp.SigstoreVerifierFacade {
        private int invocations;
        private Path lastArtifact;
        private VerificationOptions lastOptions;

        @Override
        public void verify(Path artifact, Bundle bundle, VerificationOptions options) {
            this.invocations++;
            this.lastArtifact = artifact;
            this.lastOptions = options;
        }
    }
}
