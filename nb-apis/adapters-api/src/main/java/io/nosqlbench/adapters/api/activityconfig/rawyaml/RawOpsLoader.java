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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nb.api.expr.ExprPreprocessor;
import io.nosqlbench.nb.api.expr.TemplateRewriter;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.*;
import java.util.function.Function;

public class RawOpsLoader {
    private final static Logger logger = LogManager.getLogger(RawOpsLoader.class);
    private static final ExprPreprocessor EXPRESSION_PREPROCESSOR = new ExprPreprocessor();

    public static String[] YAML_EXTENSIONS = new String[]{"yaml", "yml"};

    private final ArrayList<Function<String,String>> transformers = new ArrayList<>();

    private static LoadSettings loadSettings = LoadSettings.builder().build();
    private final Load yaml = new Load(loadSettings);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public RawOpsLoader(Function<String,String> transformer) {
        addTransformer(transformer);
    }

    public RawOpsLoader() {
        // No default transformers - template processing is handled by OpsLoader
    }

    public boolean isJson(String workload) {
        try  {
            Object canLoad = gson.fromJson(workload, Object.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void addTransformer(Function<String, String> newTransformer) {
        Collections.addAll(this.transformers, newTransformer);
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

    public RawOpsDocList loadString(final String originalData) {
        String data = applyTransforms(originalData);
        return parseYaml(data);
    }

    public List<Map<String,Object>> loadStringMap(final String originalData) {
        String data = applyTransforms(originalData);
        List<Map<String,Object>> maps = parseYamlMap(data);
        return maps;
    }

    protected String applyTransforms(String originalData) {
        String data = originalData;
        for (Function<String, String> transformer : transformers) {
            try {
                data = transformer.apply(data);
            } catch (Exception e) {
                throw new OpConfigError("Error applying string transforms to input", e);
            }
        }
        return data;
    }

    public RawOpsDocList parseYaml(String data) {
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

        int refkey = 0;
        for (RawOpsDoc rawOpsBlocks : rawOpsDocList) {
            for (RawOpsBlock rawOpsBlock : rawOpsBlocks) {
                for (RawOpDef rawOpDef : rawOpsBlock.getRawOpDefs()) {
                    rawOpDef.setRefKey(refkey++);
                }
            }
        }
        return rawOpsDocList;
    }

    private List<Map<String,Object>> parseYamlMap(String data) {
        Iterable<Object> objects = yaml.loadAllFromString(data);
        List<Map<String,Object>> maps = new ArrayList<>();

        for (Object object : objects) {
            if (object instanceof Map) {
                maps.add(new LinkedHashMap<>((Map<String,Object>)object));
            } else {
                throw new OpConfigError("Unable to coerce a non-map type to a workload structure: " + object.getClass().getCanonicalName());
            }
        }
        return maps;
    }

    /**
     * Apply template rewriting and expr processing to a string.
     * This is useful for tests that need template processing without going through
     * the full OpsLoader pipeline.
     *
     * @param source the source text with potential template variables
     * @param params parameters for template substitution
     * @return the processed text with templates resolved
     */
    public static String processTemplates(String source, Map<String, ?> params) {
        // Phase 1: Rewrite TEMPLATE syntax to expr function calls
        String templateRewritten = TemplateRewriter.rewrite(source);

        // Phase 2: Process expr expressions (including the rewritten template calls)
        return EXPRESSION_PREPROCESSOR.process(templateRewritten, null, params);
    }

}
