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

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

public class StatementsLoader {

    public static String[] YAML_EXTENSIONS = new String[]{"yaml","yml"};

    private final static Logger logger = LogManager.getLogger(StatementsLoader.class);

    public static StmtsDocList loadString(String yamlContent, Map<String,?> params) {

        StrInterpolator transformer = new StrInterpolator(params);
        RawStmtsLoader loader = new RawStmtsLoader(transformer);
        RawStmtsDocList rawDocList = loader.loadString(yamlContent);
        StmtsDocList layered = new StmtsDocList(rawDocList);
        transformer.checkpointAccesses().forEach((k,v) -> {
            layered.addTemplateVariable(k,v);
            params.remove(k);
        });
        return layered;
    }

    public static StmtsDocList loadStmt(
        Logger logger,
        String statement,
        Map<String,?> params
    ) {
        StrInterpolator transformer = new StrInterpolator(params);
        statement = transformer.apply(statement);
        RawStmtsDocList rawStmtsDocList = RawStmtsDocList.forSingleStatement(statement);
        StmtsDocList layered = new StmtsDocList(rawStmtsDocList);
        transformer.checkpointAccesses().forEach((k,v) -> {
            layered.addTemplateVariable(k,v);
            params.remove(k);
        });
        return layered;
    }

    public static StmtsDocList loadContent(
        Logger logger,
        Content<?> content,
        Map<String,String> params
    ) {
        return loadString(content.get().toString(),params);
    }

    public static StmtsDocList loadPath(
        Logger logger,
        String path,
        Map<String,?> params,
        String... searchPaths) {

        RawStmtsDocList list = null;
        Optional<Content<?>> oyaml = NBIO.all().prefix(searchPaths).name(path).extension(YAML_EXTENSIONS).first();
        String content = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + path));
        return loadString(content,params);
    }


    public static StmtsDocList loadPath(
            Logger logger,
            String path,
            String... searchPaths) {
        return loadPath(logger, path, Map.of(), searchPaths);
    }

}
