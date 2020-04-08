package io.nosqlbench.nb.api.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResolverForURL implements ContentResolver {

    public static final ContentResolver INSTANCE = new ResolverForURL();
    private final static Logger logger = LoggerFactory.getLogger(ResolverForURL.class);

    @Override
    public List<Content<?>> resolve(URI uri) {
        if (uri.getScheme()==null) {
            return null;
        }
        if (uri.getScheme().equals("http")
            || uri.getScheme().equals("https")) {
            try {
                URL url = uri.toURL();
                InputStream inputStream = url.openStream();
                logger.debug("Found accessible remote file at " + url.toString());
                return List.of(new URLContent(url, inputStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        return Collections.emptyList();
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
