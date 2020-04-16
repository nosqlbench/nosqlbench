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
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.slf4j.Logger;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RawYamlStatementLoader {

    List<Function<String, String>> stringTransformers = new ArrayList<>();

    public RawYamlStatementLoader() {
    }

    public RawYamlStatementLoader(Function<String, String> stringTransformer) {
        this.addTransformer(stringTransformer);
    }

    public RawStmtsDocList load(Logger logger, String fromPath, String... searchPaths) {
        try {

        Optional<Content<?>> oyaml = NBIO.all().prefix(searchPaths).name(fromPath).extension("yaml").first();
        String data = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + fromPath));
        data = applyTransforms(logger, data);
        return parseYaml(logger, data);
        } catch (Exception e) {
            throw new RuntimeException("error while reading file " + fromPath,e);
        }
    }

    public void addTransformer(Function<String, String> transformer) {
        stringTransformers.add(transformer);
    }

    public RawStmtsDocList load(Logger logger, Path path) {
        try {
            String yamlImg = Files.readString(path);
            return parseYaml(logger, yamlImg);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading YAML from search paths: " + e.getMessage(),e);
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

    public RawStmtsDocList loadString(Logger logger, CharSequence rawYaml) {
        String data = applyTransforms(logger, rawYaml.toString());
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
