package io.nosqlbench.nb.api.pathutil;

import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.ResolverForClasspath;
import io.nosqlbench.nb.api.content.ResolverForFilesystem;
import io.nosqlbench.nb.api.content.ResolverForURL;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolverForURLTest {

    @Test
    public void testUrlResource() {
        ResolverForURL r = new ResolverForURL();
        List<Content<?>> c = r.resolve("http://google.com");
        assertThat(c).isNotNull();
        Object location = c.get(0).getLocation();
        assertThat(location).isInstanceOf(URL.class);
        assertThat(location.toString()).isEqualTo("http://google.com");
    }

    @Test
    public void testFileResource() {
        String p = "src/test/resources/nesteddir1/nesteddir2/testcsv12.csv";
        ResolverForFilesystem r = new ResolverForFilesystem();
        List<Content<?>> c = r.resolve(p);
        assertThat(c).isNotNull();
        Object location = c.get(0).getLocation();
        assertThat(location).isInstanceOf(Path.class);
        assertThat(location.toString()).isEqualTo(p);

        String q = "nesteddir1/nesteddir2/testcsv12.csv";
        List<Content<?>> notfound = r.resolve(q);
        assertThat(notfound).isEmpty();

    }

    @Test
    public void testCPResource() {
        String cprootForTestContext = "target/test-classes/";
        String resourcePathWithinClasspathRoots = "nesteddir1/nesteddir2/testcsv12.csv";
        ResolverForClasspath r = new ResolverForClasspath();
        List<Content<?>> c = r.resolve(resourcePathWithinClasspathRoots);
        assertThat(c).isNotNull();
        Object location = c.get(0).getLocation();
        assertThat(location.toString()).isEqualTo(cprootForTestContext + resourcePathWithinClasspathRoots);

        String q = "src/test/resources/nesteddir1/nesteddir2/testcsv12.csv";
        List<Content<?>> notfound = r.resolve(q);
        assertThat(notfound).isEmpty();
    }

}
