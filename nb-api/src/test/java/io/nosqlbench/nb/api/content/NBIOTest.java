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

package io.nosqlbench.nb.api.content;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.content.NBIORelativizer;
import io.nosqlbench.api.content.NBPathsAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class NBIOTest {
    private final static Logger logger = LogManager.getLogger(NBIOTest.class);

    @Test
    public void testFullyQualifiedNameSearches() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo.bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandWildcardAndExtensionsOnly() {
        NBIO extensions = (NBIO) NBIO.all().pathname(".*").extensionSet("foo","bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains(".*.foo",".*.bar");
    }

    @Test
    public void testExpandNameAndAllSuffixesOnly() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo.bar").extensionSet("test1","test2");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar.test1","foo.bar.test2");
    }

    @Test
    public void testExpandNamesAndExtensionsIfNotExtended() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo").extensionSet("baz","beez");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains("foo.baz","foo.beez");
    }

    @Test
    public void testExpandNamesAndExtensionsAvoidsExtendedAlreadyExtended() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo.baz").extensionSet("baz","beez");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains("foo.baz","foo.beez");
    }

    @Test
    public void testExpandPrefixesAndFullName() {
        NBIO extensions = (NBIO) NBIO.all().searchPrefixes("act1","act2").pathname("foo.bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar","act1/foo.bar","act2/foo.bar");
    }

    @Test
    public void testExpandAddExtensionNotNeeded() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo.bar").extensionSet("bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().pathname("foo.bar").extensionSet(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandNamesAndSuffixes();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testExpandExtensionCosets() {
        Set<String> paths = NBIO.expandSynonymPaths(List.of("abc123.tubas"), List.of(Set.of(".foo")));
        assertThat(paths).isEqualTo(Set.of("abc123.tubas.foo"));
        paths = NBIO.expandSynonymPaths(List.of("abc123.tubas","def.123"), List.of(Set.of(".456",".789",".123")));
        assertThat(paths).isEqualTo(Set.of("abc123.tubas.123","abc123.tubas.456","abc123.tubas.789","def.123","def.456","def.789"));

    }
    @Test
    public void testExpandAddExtensionNeeded() {
        NBIO extensions = (NBIO) NBIO.all().pathname("foo").extensionSet("bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().pathname("foo").extensionSet(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandNamesAndSuffixes();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testLoadCsv1Classpath() {
        NBPathsAPI.GetPrefixes forSourceType = NBIO.classpath();
        NBPathsAPI.GetPathname nesteddir1 = forSourceType.searchPrefixes("nesteddir1");
        NBPathsAPI.GetExtensions getExtensions = nesteddir1.pathname("nesteddir2/testcsv1");
        NBPathsAPI.DoSearch forCsvExtension = getExtensions.extensionSet(".csv");
        Optional<Content<?>> testcsv1 = forCsvExtension.first();

        assertThat(testcsv1).isNotPresent();
    }

    @Test
    public void testLoadCsv1Filesystem() {
        NBPathsAPI.GetPrefixes forSourceType = NBIO.fs();
        NBPathsAPI.GetPathname nesteddir1 = forSourceType.searchPrefixes("target/test-classes/nesteddir1");
        NBPathsAPI.GetExtensions getExtensions = nesteddir1.pathname("nesteddir2/testcsv1");
        NBPathsAPI.DoSearch forCsvExtension = getExtensions.extensionSet(".csv");
        Optional<Content<?>> testcsv1 = forCsvExtension.first();

        assertThat(testcsv1).isNotPresent();

        List<Content<?>> list = forCsvExtension.list();
        assertThat(list).hasSize(0);
    }

    @Test
    public void testClasspathTestResource() {
        List<List<Content<?>>> optionals =
            NBIO.classpath().pathname("nesteddir1/nesteddir2/testcsv12.csv").resolveEach();
        assertThat(optionals).hasSize(1);
        Content<?> content = optionals.get(0).get(0);
        assertThat(content).isNotNull();
    }

    @Test
    public void testPathSearchForExtension() {
        List<Content<?>> list = NBIO.classpath()
            .searchPrefixes("nesteddir1")
            .pathname(".*.csv")
            .extensionSet("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForExtensionMissing() {
        List<Content<?>> list = NBIO.classpath()
            .searchPrefixes("nesteddir1")
            .pathname(".*")
            .extensionSet("csv")
            .list();
        assertThat(list).hasSize(2);
    }

    @Test
    public void testPathSearchForMultipleExtensions() {
        List<Content<?>> list = NBIO.classpath()
            .searchPrefixes("nesteddir1")
            .pathname(".*")
            .extensionSet("csv","txt")
            .list();
        assertThat(list).hasSize(3);
    }

    @Test
    public void testPathSearchForSuffix() {
        List<Content<?>> list = NBIO.classpath()
            .searchPrefixes("nesteddir1")
            .pathname("nesteddir2/testdata12")
            .extensionSet("txt")
            .list();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testPathSearchInDifferentVantagePoints() {
        List<Path> list = NBIO.fs()
            .searchPrefixes("target/test-classes/nesteddir1")
            .extensionSet("csv")
            .list().stream().map(Content::asPath)
            .collect(Collectors.toList());

//        assertThat(list).containsExactly(Paths.get("."));

        List<Path> relatives = NBIORelativizer.relativizePaths(Paths.get("target/test-classes/"), list);
        assertThat(relatives).hasSize(2);
    }

    @Test
    public void testLoadNamedFileAsYmlExtension() {
        List<Content<?>> list = NBIO.classpath()
            .pathname("nesteddir1/nesteddir2/testworkload1")
            .extensionSet("yml")
            .list();
        assertThat(list).hasSize(1);

        list = NBIO.classpath()
            .pathname("nesteddir1/nesteddir2/testworkload1.yml")
            .list();
        assertThat(list).hasSize(1);

        list = NBIO.classpath()
            .pathname("nesteddir1/nesteddir2/testworkload1")
            .extensionSet("abc","yml")
            .list();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testLoadAllFilesUnderPath() {
        List<Content<?>> list = null;

        list = NBIO.classpath().searchPrefixes("./").list();
        logger.debug("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSizeGreaterThan(0);

        list = NBIO.fs().searchPrefixes("./").list();
        logger.debug("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSizeGreaterThan(0);

        list = NBIO.remote().searchPrefixes("./").list();
        logger.debug("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSize(0);
    }

    @Test
    public void test() {
        List<Content<?>> list = NBIO.fs()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname("gamma.yaml").list();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testWildcardFilenameMatch() {
        NBPathsAPI.DoSearch gammasSearch = NBIO.all()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname(".*gamma")
            .extensionSet("yaml");
        List<Content<?>> gammas = gammasSearch.list();
        assertThat(gammas).hasSize(3);
    }


    @Test
    public void testSpecificFilenameMatch() {
        NBPathsAPI.DoSearch gammasSearch = NBIO.all()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname("gamma")
            .extensionSet("yaml");
        List<Content<?>> gammas = gammasSearch.list();
        assertThat(gammas).hasSize(1);
    }

    @Test
    public void matchOneWithoutTryingPrefixesFirst() {
        Content<?> result = NBIO.all()
            .searchPrefixes(
                Paths.get("target/test-classes/nesteddir1/nesteddir2").toString()
            )
            .pathname("nesteddir1/alpha-gamma.yaml")
            .one();
        assertThat(result).isNotNull();
        assertThat(result.getURI().toString()).matches(".*?[^1]/nesteddir1/alpha-gamma.yaml");
    }

    @Test
    public void matchOneFallsThroughToPrefixesSecond() {
        Content<?> result = NBIO.all()
            .searchPrefixes(
                Paths.get("target/test-classes/nesteddir1/nesteddir2").toString()
            )
            .pathname("alpha-gamma.yaml")
            .one();
        assertThat(result).isNotNull();
        assertThat(result.getURI().toString()).matches(".*?nesteddir1/nesteddir2/alpha-gamma.yaml");
    }

    @Test
    public void onlyMatchExtensionFilesWhenExtensionInCoset() {

        // This search is invalid because by providing extensions, all results
        // are required to match one of the extensions, thus the only valid
        // match here would be alpha-gamma.yaml.js
        NBPathsAPI.DoSearch invalidSearch = NBIO.all()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname("alpha-gamma.yaml")
            .extensionSet("js");

        NBPathsAPI.DoSearch validSearch1 = NBIO.all()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname("alpha-gamma")
            .extensionSet("js");

        NBPathsAPI.DoSearch validSearch2 = NBIO.all()
            .searchPrefixes(Paths.get("target/test-classes/").toString())
            .pathname("alpha-gamma.js")
            .extensionSet();


        assertThat(invalidSearch.list()).hasSize(0);
        assertThat(validSearch1.list()).hasSize(1);
        assertThat(validSearch2.list()).hasSize(1);

    }

    @Test
    public void matchFullyQualifiedPathCorrectly() {
        Path tmpdir = Paths.get("/tmp");
        if (!Files.isDirectory(tmpdir)) return;
        try {
            File tempFile = File.createTempFile(tmpdir.toString(), "testfile.csv");
            tempFile.deleteOnExit();
            String fullpath = tempFile.getAbsolutePath();
            Files.writeString(Path.of(fullpath), "COL1,COL2\n\"val1\",\"val2\"\n");
            List<Content<?>> results = NBIO.all().pathname(fullpath).list();
            assertThat(results.size()).isEqualTo(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
