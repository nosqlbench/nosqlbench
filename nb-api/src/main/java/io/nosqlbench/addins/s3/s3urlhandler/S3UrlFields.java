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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class S3UrlFields {

    public final String bucket;
    public final String key;
    public final String secretKey;
    public final String accessKey;
    private final String endpoint;

    public static S3UrlFields fromURLString(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return new S3UrlFields(url);
    }
    public S3UrlFields(URL url) {

        String accessKey = null;
        String secretKey = null;

        String userinfo = url.getUserInfo();
        if (userinfo != null) {
            String[] userfields = userinfo.split(":", 2);
            accessKey = URLDecoder.decode(userfields[0], StandardCharsets.UTF_8);
            secretKey = URLDecoder.decode(userfields[1], StandardCharsets.UTF_8);
        } else {
            String query = url.getQuery();
            if (query != null) {
                for (String qs : query.split("&")) {
                    String[] words = qs.split(":", 2);
                    if (words[0].equals("accessKey")) {
                        accessKey = URLDecoder.decode(words[1], StandardCharsets.UTF_8);
                    } else if (words[0].equals("secretKey")) {
                        secretKey = URLDecoder.decode(words[1], StandardCharsets.UTF_8);
                    }
                }
            }
        }

        // https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-bucket-intro.html

        this.accessKey = accessKey;
        this.secretKey = secretKey;

        String[] bucketAndEndpoint = url.getHost().split("\\.", 2);
        this.bucket = bucketAndEndpoint[0];
        this.endpoint = (bucketAndEndpoint.length==2) ? bucketAndEndpoint[1] : "";
        this.key = url.getPath().substring(1);
    }

    public CredentialsFingerprint credentialsFingerprint() {
        return new CredentialsFingerprint(this);
    }

    public CredentialsFingerprint getCredentialsFingerprint() {
        return null;
    }

    public static class CredentialsFingerprint {
        private final S3UrlFields fields;

        public CredentialsFingerprint(S3UrlFields fields) {
            this.fields = fields;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            S3UrlFields that = (S3UrlFields) o;

            if (!Objects.equals(fields.secretKey, that.secretKey)) return false;
            if (!Objects.equals(fields.accessKey, that.accessKey)) return false;
            return Objects.equals(fields.endpoint, that.endpoint);
        }

        @Override
        public int hashCode() {
            int result = (fields.secretKey != null ? fields.secretKey.hashCode() : 0);
            result = 31 * result + (fields.accessKey != null ? fields.accessKey.hashCode() : 0);
            result = 31 * result + (fields.endpoint != null ? fields.endpoint.hashCode() : 0);
            return result;
        }

    }


}
