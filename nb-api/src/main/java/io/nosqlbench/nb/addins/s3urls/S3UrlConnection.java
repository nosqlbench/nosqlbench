package io.nosqlbench.nb.addins.s3urls;

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
