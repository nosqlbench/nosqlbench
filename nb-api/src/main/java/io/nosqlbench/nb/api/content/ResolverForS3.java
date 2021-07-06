package io.nosqlbench.nb.api.content;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ResolverForS3 implements ContentResolver {
    public final static ResolverForS3 INSTANCE = new ResolverForS3(new S3Client());
    private final static Logger logger = LogManager.getLogger(ResolverForS3.class);
    private final S3Access s3Access;

    public ResolverForS3(S3Access s3Access) {
        this.s3Access = s3Access;
    }

    @Override
    public List<Content<?>> resolve(URI uri) {
        URIContent s3URIContent = resolveURI(uri);
        if (s3URIContent != null) {
            return List.of(s3URIContent);
        }
        return List.of();
    }

    public URIContent resolveURI(URI uri) {
        if (uri == null || uri.getScheme() == null) {
            return null;
        }
        if (uri.getScheme().equals("s3")) {
            try {
                AmazonS3URI s3URI = new AmazonS3URI(uri);
                String bucket = s3URI.getBucket();
                String key = s3URI.getKey();
                InputStream in = s3Access.openS3Object(bucket, key);
                return new URIContent(uri, in);
            } catch (AmazonServiceException e) {
                logger.warn("Failed to access S3 object at '" + uri + "': " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        return Collections.emptyList();
    }

    @Override
    public String toString() { return getClass().getSimpleName(); }

    public static class S3Client implements S3Access {
        public AmazonS3 s3InternalClient;

        @Override
        public InputStream openS3Object(String bucket, String key) {
            S3Object s3Object = getS3InternalClient().getObject(bucket, key);
            return s3Object.getObjectContent();
        }

        public synchronized AmazonS3 getS3InternalClient() {
            if (s3InternalClient == null) {
                s3InternalClient = AmazonS3ClientBuilder.defaultClient();
            }
            return s3InternalClient;
        }
    }
}
