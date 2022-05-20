package io.nosqlbench.engine.api.activityconfig.rawyaml;

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


import io.nosqlbench.engine.api.activityimpl.ActivityInitializationError;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.function.Function;

public class RawStmtsLoader {

    public static String[] YAML_EXTENSIONS = new String[]{"yaml","yml"};

    List<Function<String, String>> stringTransformers = new ArrayList<>();
    private final ArrayList<Function<String,String>> transformers = new ArrayList<>();

    public RawStmtsLoader(Function<String,String> transformer) {
        addTransformer(transformer);
    }

    public RawStmtsLoader() {
        addTransformer(new StrInterpolator());
    }

    private void addTransformer(Function<String, String> newTransformer) {
        Collections.addAll(this.transformers, newTransformer);
    }

    public RawStmtsDocList loadString(Logger logger, String data) {

        try {
            if (logger != null) logger.trace("Applying string transformer to yaml data:" + data);
            for (Function<String, String> transformer : transformers) {
                data = transformer.apply(data);
            }
        } catch (Exception e) {
            RuntimeException t = new ActivityInitializationError("Error applying string transforms to input", e);
            throw t;
        }

        return parseYaml(logger, data);
    }

    public RawStmtsDocList loadPath(
            Logger logger,
            String path,
            String... searchPaths) {

        String data = null;
        try {
            Optional<Content<?>> oyaml = NBIO.all().prefix(searchPaths).name(path).extension(YAML_EXTENSIONS).first();
            data = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + path));
            return loadString(logger, data);
        } catch (Exception e) {
            throw new RuntimeException("error while reading file " + path, e);
        }

    }

    private RawStmtsDocList parseYaml(Logger logger, String data) {
        Yaml yaml = new Yaml();
        Iterable<Object> objects = yaml.loadAll(data);
        List<RawStmtsDoc> newDocList = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Map) {
                RawStmtsDoc doc = new RawStmtsDoc();
                Map<String, Object> docfields = (Map<String, Object>) object;
                doc.setFieldsByReflection(docfields);
                if (docfields.size()>0) {
                    throw new OpConfigError("Some fields were not recognized from the yaml provided:" + docfields.keySet());
                }

                newDocList.add(doc);
            } else {
                throw new RuntimeException("Unable to coerce a non-map type to a statements yaml doc: " + object.getClass().getCanonicalName());
            }
        }
        RawStmtsDocList rawStmtsDocList = new RawStmtsDocList(newDocList);
        return rawStmtsDocList;
    }

    protected String applyTransforms(Logger logger, String data) {
        for (Function<String, String> xform : stringTransformers) {
            try {
                if (logger != null) logger.trace("Applying string transformer to yaml data:" + xform);
                data = xform.apply(data);
            } catch (Exception e) {
                RuntimeException t = new ActivityInitializationError("Error applying string transforms to input", e);
                throw t;
            }
        }
        return data;
    }

}
