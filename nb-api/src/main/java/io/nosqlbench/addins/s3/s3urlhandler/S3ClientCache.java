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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.util.WeakHashMap;

/**
 * This client cache uses the credentials provided in a URL to create
 * a fingerprint, and then creates a customized S3 client for each unique
 * instance. If these clients are not used, they are allowed to be expired
 * from the map and collected.
 */
public class S3ClientCache {

    private final WeakHashMap<S3UrlFields.CredentialsFingerprint, AmazonS3> cache = new WeakHashMap<>();

    public S3ClientCache() {
    }

    public AmazonS3 get(S3UrlFields fields) {
        AmazonS3 s3 = cache.computeIfAbsent(fields.getCredentialsFingerprint(),
            cfp -> createAuthorizedClient(fields));
        return s3;
    }

    private AmazonS3 createAuthorizedClient(S3UrlFields fields) {
        if (fields.accessKey!=null && fields.secretKey!=null) {
            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
            AWSCredentials specialcreds = new BasicAWSCredentials(fields.accessKey, fields.secretKey);
            builder = builder.withCredentials(new AWSStaticCredentialsProvider(specialcreds));
            return builder.build();
        } else {
            return AmazonS3ClientBuilder.defaultClient();
        }
    }

}
