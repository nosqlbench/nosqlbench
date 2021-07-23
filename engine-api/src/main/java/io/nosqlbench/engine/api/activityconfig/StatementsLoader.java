/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig;

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.Content;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Function;

public class StatementsLoader {

    private final static Logger logger = LogManager.getLogger(StatementsLoader.class);

    public enum Loader {
        original,
        generified
    }

    public static StmtsDocList loadString(String yamlContent) {
        RawStmtsLoader loader = new RawStmtsLoader();
        RawStmtsDocList rawDocList = loader.loadString(logger, yamlContent);
        StmtsDocList layered = new StmtsDocList(rawDocList);
        return layered;
    }

    public static StmtsDocList loadContent(
        Logger logger,
        Content<?> content,
        Map<String,String> params
    ) {
        StrInterpolator transformer = new StrInterpolator(params);
        RawStmtsLoader loader = new RawStmtsLoader(transformer);
        RawStmtsDocList rawDocList = loader.loadString(logger, content.get().toString());
        StmtsDocList layered = new StmtsDocList(rawDocList);
        for (String varname : transformer.checkpointAccesses()) {
            params.remove(varname);
        }
        return layered;
    }

    public static StmtsDocList loadContent(
        Logger logger,
        Content<?> content
    ) {
        RawStmtsLoader loader = new RawStmtsLoader();
        RawStmtsDocList rawDocList = loader.loadString(logger, content.get().toString());
        StmtsDocList layered = new StmtsDocList(rawDocList);
        return layered;
    }
//    }

    public static StmtsDocList loadPath(
            Logger logger,
            String path,
            String... searchPaths) {
        RawStmtsDocList list = null;

        StrInterpolator transformer = new StrInterpolator();
        RawStmtsLoader gloaderImpl = new RawStmtsLoader(transformer);
        list = gloaderImpl.loadPath(logger, path, searchPaths);
        return new StmtsDocList(list);
    }

    public static StmtsDocList loadStmt(
            Logger logger,
            String statement, Function<String,String> transformer) {
        String transformed = transformer.apply(statement);
        RawStmtsDocList rawStmtsDocList = RawStmtsDocList.forSingleStatement(transformed);
        return new StmtsDocList(rawStmtsDocList);

    }

    public static StmtsDocList loadPath(
            Logger logger,
            String path,
            Function<String, String> transformer,
            String... searchPaths) {
        RawStmtsDocList list = null;

        RawStmtsLoader gloaderImpl = new RawStmtsLoader(transformer);
        list = gloaderImpl.loadPath(logger, path, searchPaths);
        return new StmtsDocList(list);
    }

}
