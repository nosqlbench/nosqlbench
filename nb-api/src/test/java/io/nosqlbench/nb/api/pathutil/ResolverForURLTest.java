package io.nosqlbench.nb.api.pathutil;

import io.nosqlbench.nb.api.content.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResolverForURLTest {

    @Test
    public void testS3URIResource() {
        String s3URI = "s3://my-bucket/my-path/my-object.ext";
        String text = "my object data\n";
        S3Access mockS3Access = mock(S3Access.class);
        when(mockS3Access.openS3Object("my-bucket", "my-path/my-object.ext"))
            .thenReturn(new ByteArrayInputStream(text.getBytes()));
        ResolverForS3 r = new ResolverForS3(mockS3Access);
        List<Content<?>> content = r.resolve(s3URI);
        assertThat(content).isNotNull();
        assertThat(content).isNotEmpty();
        Content<?> c = content.get(0);
        Object location = c.getLocation();
        assertThat(location).isInstanceOf(URI.class);
        assertThat(location.toString()).isEqualTo(s3URI);
        String actualText = c.getCharBuffer().toString();
        assertThat(actualText).isEqualTo(text);
    }

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
