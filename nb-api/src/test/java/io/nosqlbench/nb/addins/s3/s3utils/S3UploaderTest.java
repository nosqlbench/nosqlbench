package io.nosqlbench.nb.addins.s3.s3utils;

import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import io.nosqlbench.nb.addins.s3.s3urlhandler.S3UrlStreamHandlerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class S3UploaderTest {

    @Disabled
    @Test
    public void testDirUpload() {
        Path path = Path.of("src/test/resources/nesteddir1");
        S3UploaderDemo ul = new S3UploaderDemo();
        MultipleFileUpload mfu = ul.syncup(path, S3UrlStreamHandlerTest.bucketName, "test-prefix");
        System.out.println(mfu);
    }

}
