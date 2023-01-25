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

package io.nosqlbench.engine.api.activityconfig;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawOpsDocList;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

public class OpsLoader {

    private final static Logger logger = LogManager.getLogger(OpsLoader.class);

    public static String[] YAML_EXTENSIONS = new String[]{"yaml","yml", "json", "jsonnet"};

    public static OpsDocList loadContent(Content<?> content, Map<String,String> params) {
        return loadString(content.get().toString(),params);
    }

    public static OpsDocList loadPath(String path, Map<String,?> params, String... searchPaths) {
        RawOpsDocList list = null;
        Optional<Content<?>> oyaml = NBIO.all().prefix(searchPaths).name(path).extension(YAML_EXTENSIONS).first();
        String content = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + path));
        return loadString(content,params);
    }
    public static OpsDocList loadPath(
            String path,
            String... searchPaths) {
        return loadPath(path, Map.of(), searchPaths);
    }

    public static OpsDocList loadString(String yamlContent, Map<String,?> params) {

        StrInterpolator transformer = new StrInterpolator(params);
        RawOpsLoader loader = new RawOpsLoader(transformer);
        RawOpsDocList rawDocList = loader.loadString(yamlContent);
        OpsDocList layered = new OpsDocList(rawDocList);
        transformer.checkpointAccesses().forEach((k,v) -> {
            layered.addTemplateVariable(k,v);
            params.remove(k);
        });
        return layered;
    }

    public static OpsDocList loadStmt(String statement, Map<String,?> params) {
        StrInterpolator transformer = new StrInterpolator(params);
        statement = transformer.apply(statement);
        RawOpsDocList rawOpsDocList = RawOpsDocList.forSingleStatement(statement);
        OpsDocList layered = new OpsDocList(rawOpsDocList);
        transformer.checkpointAccesses().forEach((k,v) -> {
            layered.addTemplateVariable(k,v);
            params.remove(k);
        });
        return layered;
    }



}
