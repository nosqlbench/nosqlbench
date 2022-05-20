package io.nosqlbench.nb.api.content;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class NBIOTest {

    @Test
    public void testFullyQualifiedNameSearches() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandWildcardAndExtensionsOnly() {
        NBIO extensions = (NBIO) NBIO.all().name(".*").extension("foo","bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains(".*.foo",".*.bar");
    }

    @Test
    public void testExpandNameAndAllSuffixesOnly() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar").extension("test1","test2");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar.test1","foo.bar.test2");
    }

    @Test
    public void testExpandNamesAndExtensionsIfNotExtended() {
        NBIO extensions = (NBIO) NBIO.all().name("foo").extension("baz","beez");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains("foo.baz","foo.beez");
    }

    @Test
    public void testExpandNamesAndExtensionsAvoidsExtendedAlreadyExtended() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.baz").extension("baz","beez");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).contains("foo.baz","foo.beez");
    }

    @Test
    public void testExpandPrefixesAndFullName() {
        NBIO extensions = (NBIO) NBIO.all().prefix("act1","act2").name("foo.bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
    }

    @Test
    public void testExpandAddExtensionNotNeeded() {
        NBIO extensions = (NBIO) NBIO.all().name("foo.bar").extension("bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().name("foo.bar").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandNamesAndSuffixes();
        assertThat(searchesDot).containsExactly("foo.bar");
    }

    @Test
    public void testExpandAddExtensionNeeded() {
        NBIO extensions = (NBIO) NBIO.all().name("foo").extension("bar");
        LinkedHashSet<String> searches = extensions.expandNamesAndSuffixes();
        assertThat(searches).containsExactly("foo.bar");
        NBIO extensionsDot = (NBIO) NBIO.all().name("foo").extension(".bar");
        LinkedHashSet<String> searchesDot = extensionsDot.expandNamesAndSuffixes();
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

        List<Content<?>> list = forCsvExtension.list();
        assertThat(list).hasSize(0);
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

    @Test
    public void testPathSearchInDifferentVantagePoints() {
        List<Path> list = NBIO.fs()
            .prefix("target/test-classes/nesteddir1")
            .extension("csv")
            .list().stream().map(Content::asPath)
            .collect(Collectors.toList());

//        assertThat(list).containsExactly(Paths.get("."));

        List<Path> relatives = NBIORelativizer.relativizePaths(Paths.get("target/test-classes/"), list);
        assertThat(relatives).hasSize(2);
    }

    @Test
    public void testLoadNamedFileAsYmlExtension() {
        List<Content<?>> list = NBIO.classpath()
            .name("nesteddir1/nesteddir2/testworkload1")
            .extension("yml")
            .list();
        assertThat(list).hasSize(1);

        list = NBIO.classpath()
            .name("nesteddir1/nesteddir2/testworkload1.yml")
            .list();
        assertThat(list).hasSize(1);

        list = NBIO.classpath()
            .name("nesteddir1/nesteddir2/testworkload1")
            .extension("abc","yml")
            .list();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testLoadAllFilesUnderPath() {
        List<Content<?>> list = null;

        list = NBIO.classpath().prefix("./").list();
        System.out.println("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSizeGreaterThan(0);

        list = NBIO.fs().prefix("./").list();
        System.out.println("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSizeGreaterThan(0);

        list = NBIO.remote().prefix("./").list();
        System.out.println("found " + list.size() + " entries for path '.'");
        assertThat(list).hasSize(0);
    }

    @Test
    public void test() {
        List<Content<?>> list = NBIO.fs()
            .prefix(Paths.get("target/test-classes/").toString())
            .name("gamma.yaml").list();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testWildcardFilenameMatch() {
        NBPathsAPI.DoSearch gammasSearch = NBIO.all()
            .prefix(Paths.get("target/test-classes/").toString())
            .name(".*gamma")
            .extension("yaml");
        List<Content<?>> gammas = gammasSearch.list();
        assertThat(gammas).hasSize(3);
    }


    @Test
    public void testSpecificFilenameMatch() {
        NBPathsAPI.DoSearch gammasSearch = NBIO.all()
            .prefix(Paths.get("target/test-classes/").toString())
            .name("gamma")
            .extension("yaml");
        List<Content<?>> gammas = gammasSearch.list();
        assertThat(gammas).hasSize(1);
    }

    @Test
    public void onlyMatchExtensionFilesWhenExtensionsProvided() {

        // This search is invalid because by providing extensions, all results
        // are required to match one of the extensions, thus the only valid
        // match here would be alpha-gamma.yaml.js
        NBPathsAPI.DoSearch invalidSearch = NBIO.all()
            .prefix(Paths.get("target/test-classes/").toString())
            .name("alpha-gamma.yaml")
            .extension("js");

        NBPathsAPI.DoSearch validSearch1 = NBIO.all()
            .prefix(Paths.get("target/test-classes/").toString())
            .name("alpha-gamma")
            .extension("js");

        NBPathsAPI.DoSearch validSearch2 = NBIO.all()
            .prefix(Paths.get("target/test-classes/").toString())
            .name("alpha-gamma.js")
            .extension();


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
            Files.write(Path.of(fullpath), "COL1,COL2\n\"val1\",\"val2\"\n".getBytes(StandardCharsets.UTF_8));
            List<Content<?>> results = NBIO.all().name(fullpath).list();
            assertThat(results.size()).isEqualTo(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
