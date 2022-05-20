package io.nosqlbench.engine.extensions.s3uploader;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
