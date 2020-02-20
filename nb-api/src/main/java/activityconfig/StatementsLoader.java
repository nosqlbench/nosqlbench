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

package activityconfig;

import activityconfig.rawyaml.RawStmtsDocList;
import activityconfig.rawyaml.RawYamlStatementLoader;
import activityconfig.yaml.StmtsDocList;
import org.slf4j.Logger;

import java.util.function.Function;

public class StatementsLoader {

    public static StmtsDocList load(Logger logger, String path, String... searchPaths) {
        RawYamlStatementLoader loader = new RawYamlStatementLoader();
        RawStmtsDocList rawDocList = loader.load(logger, path, searchPaths);
        StmtsDocList layered = new StmtsDocList(rawDocList);
        return layered;
    }

    public static StmtsDocList load(Logger logger, String path, Function<String, String> transformer, String... searchPaths) {
        RawYamlStatementLoader loader = new RawYamlStatementLoader(transformer);
        RawStmtsDocList rawDocList = loader.load(logger, path, searchPaths);
        StmtsDocList layered = new StmtsDocList(rawDocList);
        return layered;
    }

}
