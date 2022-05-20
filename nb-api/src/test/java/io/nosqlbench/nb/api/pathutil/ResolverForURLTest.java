package io.nosqlbench.nb.api.pathutil;

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
