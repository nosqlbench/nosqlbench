/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.extensions.s3uploader;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.config.LabeledScenarioContext;
import io.nosqlbench.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.metadata.ScenarioMetadata;
import io.nosqlbench.api.metadata.ScenarioMetadataAware;
import org.apache.logging.log4j.Logger;

@Service(value = ScriptingExtensionPluginInfo.class, selector = "s3")
public class S3UploaderPluginData implements ScriptingExtensionPluginInfo<S3Uploader>, ScenarioMetadataAware {
    private ScenarioMetadata scenarioMetadata;

    @Override
    public String getDescription() {
        return "Allow for uploading or downloading a directory from S3";
    }

    @Override
    public S3Uploader getExtensionObject(final Logger logger, final NBBaseComponent baseComponent, final LabeledScenarioContext scriptContext) {
        final S3Uploader uploader = new S3Uploader(logger, baseComponent, scriptContext);
        ScenarioMetadataAware.apply(uploader, this.scenarioMetadata);
        return uploader;
    }

    @Override
    public void setScenarioMetadata(final ScenarioMetadata metadata) {
        scenarioMetadata = metadata;
    }
}
