package io.nosqlbench.nb.addins.s3.s3urlhandler;

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


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class S3UrlStreamHandlerTest {

    public static String bucketName = "nb-extension-test";
    public static String keyName = "key-name";
    public static String testValue = "test-value";

    /**
     * This test requires that you have credentials already configured on your local system
     * for S3. It creates an object using the s3 client directly, then uses a generic
     * URL method to access and verify the contents.
     */
    @Disabled
    @Test
    public void sanityCheckS3UrlHandler() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();


        Bucket bucket = null;

        if (!client.doesBucketExistV2(bucketName)) {
            bucket = client.createBucket(bucketName);
        }
        PutObjectResult putObjectResult = client.putObject(bucketName, keyName, testValue);
        assertThat(putObjectResult).isNotNull();

        try {
            URL url = new URL("s3://"+bucketName+"/"+keyName);
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            assertThat(line).isEqualTo(testValue);
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
