/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.addins.s3.s3utils;

import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import io.nosqlbench.nb.addins.s3.s3urlhandler.S3UrlStreamHandlerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class S3UploaderTest {

    @Disabled
    @Test
    public void testDirUpload() {
        Path path = Path.of("src/test/resources/nesteddir1");
        S3UploaderDemo ul = new S3UploaderDemo();
        MultipleFileUpload mfu = ul.syncup(path, S3UrlStreamHandlerTest.bucketName, "test-prefix");
        System.out.println(mfu);
    }

}
