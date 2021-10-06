package io.nosqlbench.nb.addins.s3urls;

import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;

public class S3UrlStreamHandler extends URLStreamHandler {

    private final S3ClientCache clientCache;
    private final String protocol;

    public S3UrlStreamHandler(S3ClientCache clientCache, String protocol) {
        this.clientCache = clientCache;
        this.protocol = protocol;
    }

    @Override
    protected S3UrlConnection openConnection(URL url) throws IOException {
        return new S3UrlConnection(clientCache, url);
    }
}
