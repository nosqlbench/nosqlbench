package io.nosqlbench.engine.extensions.s3uploader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.nb.addins.s3.s3urlhandler.S3ClientCache;
import io.nosqlbench.nb.addins.s3.s3urlhandler.S3UrlFields;
import io.nosqlbench.nb.api.NBEnvironment;
import io.nosqlbench.nb.api.metadata.ScenarioMetadata;
import io.nosqlbench.nb.api.metadata.ScenarioMetadataAware;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class S3Uploader implements ScenarioMetadataAware {
    private final Logger logger;
    private final MetricRegistry metricRegistry;
    private final ScriptContext scriptContext;
    private ScenarioMetadata scenarioMetadata;

    public S3Uploader(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.scriptContext = scriptContext;
    }

    /**
     * Upload the local file path to the specified S3 URL, then return the URL of the bucket
     * in its fully expanded form. See the details on token expansions in the s3.md help docs.
     * @param localFilePath The path to the local directory
     * @param urlTemplate A template that is expanded to a valid S3 URL
     * @return The fully expanded name of the URL used for upload
     */
    public String uploadDirToUrl(String localFilePath, String urlTemplate) {
        return uploadDirToUrlTokenized(localFilePath, urlTemplate, Map.of());
    }

    /**
     * Upload the local file path to the specified S3 URL, then return the URL of the bucket
     * in its fully expanded form. See the details on token expansions in the s3.md help docs.
     * Any params which are provided supersede the normally provided values from the system.
     * @param localFilePath The path to the local directory
     * @param urlTemplate A template that is expanded to a valid S3 URL
     * @param params Additional token expansions which will take precedence over other available values.
     * @return The fully expanded name of the URL used for upload
     */
    public String uploadDirToUrlTokenized(String localFilePath, String urlTemplate, Map<String,String> params) {


        Path sourcePath = Path.of(localFilePath);
        if (!FileSystems.getDefault().equals(sourcePath.getFileSystem())) {
            throw new RuntimeException("The file must reside on the default filesystem to be uploaded by S3.");
        }
        if (!Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("path '" + sourcePath + "' is not a directory.");
        }
        File sourceDir = sourcePath.toFile();

        Map<String,String> combined = new LinkedHashMap<>(params);
        combined.putAll(scenarioMetadata.asMap());
        String url = NBEnvironment.INSTANCE.interpolateWithTimestamp(
            urlTemplate,
                scenarioMetadata.getStartedAt(),
                combined
            )
            .orElseThrow();
        logger.debug("S3 composite URL is '" + url + "'");

        S3UrlFields fields = S3UrlFields.fromURLString(url);
        S3ClientCache s3ClientCache = new S3ClientCache();
        AmazonS3 s3 = s3ClientCache.get(fields);
        TransferManager xfers = TransferManagerBuilder.standard().withS3Client(s3).build();
        String prefix = fields.key;
        MultipleFileUpload mfu = xfers.uploadDirectory(fields.bucket, prefix, sourceDir, true);
        try {
            mfu.waitForCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException("Multi-file upload was interrupted.");
        }
        return url;
    }

    @Override
    public void setScenarioMetadata(ScenarioMetadata metadata) {
        this.scenarioMetadata = metadata;
    }
}
