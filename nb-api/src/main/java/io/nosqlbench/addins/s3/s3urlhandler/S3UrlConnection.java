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

package io.nosqlbench.addins.s3.s3urlhandler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class S3UrlConnection extends URLConnection {

    private final S3ClientCache clientCache;

    protected S3UrlConnection(S3ClientCache clientCache, URL url) {
        super(url);
        this.clientCache = clientCache;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        S3UrlFields fields = new S3UrlFields(url);
        AmazonS3 s3 = clientCache.get(fields);
        S3Object object = s3.getObject(fields.bucket, fields.key);
        return object.getObjectContent();
    }

    @Override
    public void connect() throws IOException {
    }
}
