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
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import io.nosqlbench.nb.addins.s3.s3urlhandler.S3ClientCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * This is a generic s3 directory uploader which is neither a scripting plugin nor a standard URL handler.
 */
public class S3UploaderDemo {

    private final S3ClientCache clientCache = new S3ClientCache();

    private static final Logger logger = LogManager.getLogger(S3UploaderDemo.class);

    public MultipleFileUpload syncup(Path sourcePath, String bucket, String prefix) {

        if (!FileSystems.getDefault().equals(sourcePath.getFileSystem())) {
            throw new RuntimeException("The file must reside on the default filesystem to be uploaded by S3.");
        }

        if (!Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("path '" + sourcePath + "' is not a directory.");
        }

        TransferManager tm = TransferManagerBuilder.defaultTransferManager();
        MultipleFileUpload mfu = tm.uploadDirectory(bucket, prefix, sourcePath.toFile(), true);
        try {
            mfu.waitForCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException("Multi-file upload was interrupted!");
        }
        tm.shutdownNow();
        return mfu;
    }

}
