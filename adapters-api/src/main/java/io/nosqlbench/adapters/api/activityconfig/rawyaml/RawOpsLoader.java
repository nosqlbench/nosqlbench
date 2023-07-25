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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.adapters.api.templating.StrInterpolator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.*;
import java.util.function.Function;

public class RawOpsLoader {
    private final static Logger logger = LogManager.getLogger(RawOpsLoader.class);

    public static String[] YAML_EXTENSIONS = new String[]{"yaml","yml"};

    private final ArrayList<Function<String,String>> transformers = new ArrayList<>();

    public RawOpsLoader(Function<String,String> transformer) {
        addTransformer(transformer);
    }

    public RawOpsLoader() {
        addTransformer(new StrInterpolator());
    }

    private void addTransformer(Function<String, String> newTransformer) {
        Collections.addAll(this.transformers, newTransformer);
    }

    public RawOpsDocList loadString(final String originalData) {
        logger.trace(() -> "Applying string transformer to yaml data:" + originalData);
        String data = originalData;
        try {
            for (Function<String, String> transformer : transformers) {
                data = transformer.apply(data);
            }
        } catch (Exception e) {
            RuntimeException t = new OpConfigError("Error applying string transforms to input", e);
            throw t;
        }

        return parseYaml(data);
    }

    public RawOpsDocList loadPath(
            String path,
            String... searchPaths) {

        String data = null;
        try {
            Optional<Content<?>> oyaml = NBIO.all().searchPrefixes(searchPaths).pathname(path).extensionSet(YAML_EXTENSIONS).first();
            data = oyaml.map(Content::asString).orElseThrow(() -> new BasicError("Unable to load " + path));
            return loadString(data);
        } catch (Exception e) {
            throw new RuntimeException("error while reading file " + path, e);
        }

    }

    public RawOpsDocList parseYaml(String data) {
        LoadSettings loadSettings = LoadSettings.builder().build();
        Load yaml = new Load(loadSettings);
        Iterable<Object> objects = yaml.loadAllFromString(data);
        List<RawOpsDoc> newDocList = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Map) {
                RawOpsDoc doc = new RawOpsDoc();
                Map<String, Object> docfields = (Map<String, Object>) object;
                doc.setFieldsByReflection(docfields);
                if (docfields.size()>0) {
                    throw new OpConfigError("Some fields were not recognized from the yaml provided:" + docfields.keySet());
                }

                newDocList.add(doc);
            } else {
                throw new RuntimeException("Unable to coerce a non-map type to a workload structure: " + object.getClass().getCanonicalName());
            }
        }
        RawOpsDocList rawOpsDocList = new RawOpsDocList(newDocList);
        return rawOpsDocList;
    }

}
