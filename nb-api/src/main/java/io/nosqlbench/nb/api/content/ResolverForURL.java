package io.nosqlbench.nb.api.content;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResolverForURL implements ContentResolver {

    public static final ContentResolver INSTANCE = new ResolverForURL();
    private final static Logger logger = LogManager.getLogger(ResolverForURL.class);

    @Override
    public List<Content<?>> resolve(URI uri) {
        List<Content<?>> contents = new ArrayList<>();
        URLContent urlContent = resolveURI(uri);
        if (urlContent!=null) {
            return List.of(urlContent);
        } else {
            return List.of();
        }
    }

    private URLContent resolveURI(URI uri) {
        if (uri.getScheme() == null) {
            return null;
        }
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            try {
                URL url = uri.toURL();
                InputStream inputStream = url.openStream();
                logger.debug("Found accessible remote file at " + url);
                return new URLContent(url, inputStream);
            } catch (IOException e) {
                logger.warn("Unable to find content at URI '" + uri + "', this often indicates a configuration error.");
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        return Collections.emptyList();
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
