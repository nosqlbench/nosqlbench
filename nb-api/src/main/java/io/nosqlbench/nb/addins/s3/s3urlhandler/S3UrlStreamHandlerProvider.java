package io.nosqlbench.nb.addins.s3.s3urlhandler;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.annotations.Maturity;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

@Service(value = URLStreamHandlerProvider.class, selector = "s3", maturity = Maturity.Verified)
public class S3UrlStreamHandlerProvider extends URLStreamHandlerProvider {

    private final S3ClientCache clientCache = new S3ClientCache();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("s3".equals(protocol)) {
            return new S3UrlStreamHandler(clientCache, protocol);
        }
        return null;
    }

}
