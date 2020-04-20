package io.nosqlbench.nb.api.content;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NBIOTest {

    @Test
    public void testFullyQualifiedNameSearches() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandWildcardAndExtensionsOnly() {
        NBIO extensions = (NBIO) NBIO.all().name(".*").extension("foo","bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly(".*.foo",".*.bar");
    }

    @Test
    public void testExpandNameOnly() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar").extension();
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandNamesAndExtensions() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar").extension("baz","beez");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar.baz","foo.bar.beez");

    }

    @Test
    public void testExpandPrefixesAndFullName() {
        NBIO extensions = (NBIO) NBIO.all().prefix("act1","act2").name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandAddExtensionNotNeeded() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar").extension("bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().name("foo.bar").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandSearches();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testExpandAddExtensionNeeded() {
        NBIO extensions = (NBIO) NBIO.all().name("foo").extension("bar");
        LinkedHashSet<String> searches = extensions.expandSearches();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().name("foo").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandSearches();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testLoadCsv1Classpath() {
        NBPathsAPI.GetPrefix forSourceType = NBIO.classpath();
        NBPathsAPI.GetName nesteddir1 = forSourceType.prefix("nesteddir1");
        NBPathsAPI.GetExtension getExtension = nesteddir1.name("nesteddir2/testcsv1");
        NBPathsAPI.DoSearch forCsvExtension = getExtension.extension(".csv");
        Optional<Content<?>> testcsv1 = forCsvExtension.first();

        assertThat(testcsv1).isNotPresent();
    }

    @Test
    public void testLoadCsv1Filesystem() {
        NBPathsAPI.GetPrefix forSourceType = NBIO.fs();
        NBPathsAPI.GetName nesteddir1 = forSourceType.prefix("target/test-classes/nesteddir1");
        NBPathsAPI.GetExtension getExtension = nesteddir1.name("nesteddir2/testcsv1");
        NBPathsAPI.DoSearch forCsvExtension = getExtension.extension(".csv");
        Optional<Content<?>> testcsv1 = forCsvExtension.first();

        assertThat(testcsv1).isNotPresent();
    }

    @Test
    public void testClasspathTestResource() {
        List<List<Content<?>>> optionals =
            NBIO.classpath().name("nesteddir1/nesteddir2/testcsv12.csv").resolveEach();
        assertThat(optionals).hasSize(1);
        Content<?> content = optionals.get(0).get(0);
        assertThat(content).isNotNull();
    }

    @Test
    public void testPathSearchForExtension() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .name(".*.csv")
            .extension("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForExtensionMissing() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .name(".*")
            .extension("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForMultipleExtensions() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .name(".*")
            .extension("csv","txt")
            .list();
        assertThat(list).hasSize(3);
    }

    @Test
    public void testPathSearchForSuffix() {
        List<Content<?>> list = NBIO.classpath()
            .prefix("nesteddir1")
            .name("nesteddir2/testdata12")
            .extension("txt")
            .list();
        assertThat(list).hasSize(1);
    }

}
