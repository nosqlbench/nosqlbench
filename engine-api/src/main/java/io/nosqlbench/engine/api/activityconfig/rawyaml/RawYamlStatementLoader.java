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

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.engine.api.activityconfig.snakecharmer.SnakeYamlCharmer;
import io.nosqlbench.engine.api.activityimpl.ActivityInitializationError;
import io.nosqlbench.engine.api.util.NosqlBenchFiles;
import org.slf4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawYamlStatementLoader {

    List<Function<String, String>> stringTransformers = new ArrayList<>();

    public RawYamlStatementLoader() {
    }

    public RawYamlStatementLoader(Function<String, String> stringTransformer) {
        this.addTransformer(stringTransformer);
    }

    public RawStmtsDocList load(Logger logger, String fromPath, String... searchPaths) {
        String data = loadRawFile(logger, fromPath, searchPaths);
        data = applyTransforms(logger, data);
        return parseYaml(logger, data);
    }

    public void addTransformer(Function<String, String> transformer) {
        stringTransformers.add(transformer);
    }

    protected String loadRawFile(Logger logger, String fromPath, String... searchPaths) {
        InputStream stream = NosqlBenchFiles.findRequiredStreamOrFile(fromPath, "yaml", searchPaths);
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while reading YAML from search paths:" + Arrays.toString(searchPaths) + ":" + e.getMessage(), e
            );
        }
    }

    protected String applyTransforms(Logger logger, String data) {
        for (Function<String, String> xform : stringTransformers) {
            try {
                if (logger != null) logger.debug("Applying string transformer to yaml data:" + xform);
                data = xform.apply(data);
            } catch (Exception e) {
                RuntimeException t = new ActivityInitializationError("Error applying string applyTransforms to input", e);
                if (logger != null) logger.error(t.getMessage(), t);
                throw t;
            }
        }
        return data;
    }

    protected RawStmtsDocList parseYaml(Logger logger, String data) {
        Yaml yaml = getCustomYaml();

        try {
            Iterable<Object> objects = yaml.loadAll(data);
            List<RawStmtsDoc> stmtListList = new ArrayList<>();
            for (Object object : objects) {
                RawStmtsDoc tgsd = (RawStmtsDoc) object;
                stmtListList.add(tgsd);
            }
            return new RawStmtsDocList(stmtListList);
        } catch (Exception e) {
            if (logger != null) logger.error("yaml-construction-error: Error building configuration:"
                    + e.getMessage() + "" +
                    " For more details on this error see the " +
                    "troubleshooting section of the YAML format docs " +
                    "for yaml-construction-error.", e);
            throw e;
        }
    }

    protected Yaml getCustomYaml() {

        SnakeYamlCharmer charmer = new SnakeYamlCharmer(RawStmtsDoc.class);
        charmer.addHandler(StatementsOwner.class, "statements", new StatementsReader());
        charmer.addHandler(StatementsOwner.class, "statement", new StatementsReader());

        TypeDescription tds = new TypeDescription(RawStmtsDoc.class);
        tds.addPropertyParameters("blocks", RawStmtsBlock.class);
        charmer.addTypeDescription(tds);

        return new Yaml(charmer);
    }


    protected RawStmtsDocList loadString(Logger logger, String rawYaml) {
        String data = applyTransforms(logger, rawYaml);
        return parseYaml(logger, data);
    }

    private class StatementsReader implements SnakeYamlCharmer.FieldHandler {
        @Override
        public void handleMapping(Object object, Object nodeTuple) {
            //System.out.println("Handling mapping for" + object +", nodes:" + nodeTuple);
            if (object instanceof StatementsOwner) {
                ((StatementsOwner) object).setByObject(nodeTuple);
            }

        }
    }
}
