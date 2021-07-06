package io.nosqlbench.nb.api.content;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class URLContent extends StreamContent<URL> {
    private final URL url;

    public URLContent(URL url, InputStream inputStream) {
        super(url, inputStream);
        this.url = url;
    }

    @Override
    public URI getURI() {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLContent that = (URLContent) o;
        return Objects.equals(url, that.url);}

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public String toString() {
        return "URLContent{" + getURI().toString() + "}";
    }
}
