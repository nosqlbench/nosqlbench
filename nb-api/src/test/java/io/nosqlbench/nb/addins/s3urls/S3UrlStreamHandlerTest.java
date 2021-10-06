package io.nosqlbench.nb.addins.s3urls;

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

    /**
     * This test requires that you have credentials already configured on your local system
     * for S3. It creates an object using the s3 client directly, then uses a generic
     * URL method to access and verify the contents.
     */
    @Disabled
    @Test
    public void sanityCheckS3UrlHandler() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();

        String bucketName = "nb-extension-test";
        String keyName = "key-name";
        String testValue = "test-value";

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
