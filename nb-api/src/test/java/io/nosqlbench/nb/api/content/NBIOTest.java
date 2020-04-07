package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.content.fluent.NBPathsAPI;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NBIOTest {

    @Test
    public void testSimpleSearch() {
        NBIO extensions = (NBIO) NBIO.all().exact().name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testSearchPaths() {
        NBIO extensions = (NBIO) NBIO.all().prefix("act1","act2").exact().name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar","act1/foo.bar","act2/foo.bar");
    }

    @Test
    public void testDefaultExtensionOn() {
        NBIO extensions = (NBIO) NBIO.all().exact().name("foo.bar").extension("bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().exact().name("foo.bar").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandSearches();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testDefaultExtensionOff() {
        NBIO extensions = (NBIO) NBIO.all().exact().name("foo").extension("bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo","foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().exact().name("foo").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandSearches();
        assertThat(searchesDot).containsExactly("foo","foo.bar");
    }

    @Test
    public void testLoadCsv1() {
        NBPathsAPI.ForContentSource forSourceType = NBIO.fs();
        NBPathsAPI.ForPrefix forPrefix = forSourceType.prefix("nesteddir1");
        NBPathsAPI.ForName forName = forPrefix.name("nesteddir2/testcsv1");
        NBPathsAPI.ForExtension forCsvExtension = forName.extension(".csv");
        Optional<Content<?>> testcsv1 = forCsvExtension.first();

        assertThat(testcsv1).isNotPresent();
    }

    @Test
    public void testClasspathTestResource() {
        List<Optional<Content<?>>> optionals =
            NBIO.classpath().name("nesteddir1/nesteddir2/testcsv12.csv").resolveEach();
        assertThat(optionals).hasSize(1);
        Content<?> content = optionals.get(0).get();
        assertThat(content).isNotNull();
    }

    @Test
    public void testPathSearchForExtension() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .regex()
            .name(".*\\.csv")
            .extension("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForExtensionMissing() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .regex()
            .name(".*")
            .extension("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForMultipleExtensions() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .regex()
            .name(".*")
            .extension("csv","txt")
            .list();
        assertThat(list).hasSize(3);
    }

    @Test
    public void testPathSearchForSuffix() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .regex()
            .name("nesteddir2/testdata12")
            .extension("txt")
            .list();
        assertThat(list).hasSize(1);
    }

}
