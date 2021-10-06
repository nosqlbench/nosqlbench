package io.nosqlbench.nb.addins.s3urls;

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
        AmazonS3 s3 = cache.computeIfAbsent(fields.credentialsFingerprint(),
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
