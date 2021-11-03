package io.nosqlbench.engine.extensions.s3uploader;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.metadata.ScenarioMetadata;
import io.nosqlbench.nb.api.metadata.ScenarioMetadataAware;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

@Service(value = ScriptingPluginInfo.class, selector = "s3")
public class S3UploaderPluginData implements ScriptingPluginInfo<S3Uploader>, ScenarioMetadataAware {
    private ScenarioMetadata scenarioMetadata;

    @Override
    public String getDescription() {
        return "Allow for uploading or downloading a directory from S3";
    }

    @Override
    public S3Uploader getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        S3Uploader uploader = new S3Uploader(logger, metricRegistry, scriptContext);
        ScenarioMetadataAware.apply(uploader,scenarioMetadata);
        return uploader;
    }

    @Override
    public void setScenarioMetadata(ScenarioMetadata metadata) {
        this.scenarioMetadata = metadata;
    }
}
