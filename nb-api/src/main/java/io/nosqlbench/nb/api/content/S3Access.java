package io.nosqlbench.nb.api.content;

import java.io.InputStream;

public interface S3Access {
    InputStream openS3Object(String bucket, String key);
}
