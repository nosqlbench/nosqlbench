package io.nosqlbench.nb.api.content;

import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

public class URIContent extends StreamContent<URI> {
    private final URI uri;

    public URIContent(URI uri, InputStream inputStream) {
        super(uri, inputStream);
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URIContent that = (URIContent) o;
        return Objects.equals(uri, that.uri);}

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    public String toString() {
        return "URIContent{" + getURI().toString() + "}";
    }
}
