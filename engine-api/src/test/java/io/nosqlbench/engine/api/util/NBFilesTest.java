/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.util;

import io.nosqlbench.nb.api.pathutil.NBFiles;
import org.junit.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NBFilesTest {

    @Test
    public void testNestedClasspathLoading() {
        Optional<InputStream> optionalStreamOrFile = NBFiles.findOptionalStreamOrFile("nested/testfile", "txt", "activities");
        assertThat(optionalStreamOrFile).isPresent();
    }

//    @Test
//    public void testUrlResourceSearchSanity() {
//        String url="https://google.com/robots";
//        Optional<InputStream> inputStream = NosqlBenchFiles.findOptionalStreamOrFile(url,"txt","activity");
//        assertThat(inputStream).isPresent();
//    }
//
//    @Test
//    public void testUrlResourceLoading() {
//        String url="https://google.com/";
//        Optional<InputStream> inputStream = NosqlBenchFiles.getInputStream(url);
//        assertThat(inputStream).isPresent();
//    }
}
