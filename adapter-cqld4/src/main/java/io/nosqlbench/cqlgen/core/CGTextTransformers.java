/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.cqlgen.core;

import io.nosqlbench.cqlgen.api.CGTextTransformer;
import io.nosqlbench.cqlgen.transformers.CGNameObfuscator;
import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CGTextTransformers implements Consumer<List<Map<String, ?>>>, Supplier<List<CGTextTransformer>> {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/text-transformers");
    private final List<CGTextTransformer> transformers = new ArrayList<>();

    @Override
    public List<CGTextTransformer> get() {
        return transformers;
    }

    @Override
    public void accept(List<Map<String, ?>> configs) {
        List<CGTextTransformer> transformers = new ArrayList<>();

        for (Map<String, ?> cfgmap : configs) {

            // Instantiate Transformer

            String classname = cfgmap.get("class").toString();
            if (!classname.contains(".")) {
                String newname = CGNameObfuscator.class.getPackageName() + "." + classname;
                logger.info("qualified transformer '" + classname + "' as '" + newname + "'");
                classname = newname;
            }
            Class<?> txclass = null;
            CGTextTransformer transformer = null;
            try {
                txclass = Class.forName(classname);
                Constructor<?> ctor = txclass.getConstructor();
                Object instance = ctor.newInstance();
                if (instance instanceof CGTextTransformer t) {
                    transformer = t;
                } else {
                    throw new RuntimeException("Object " + instance.getClass().getName() + " is not a " + CGTextTransformer.class.getName());
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }


            // Configure Transformer IFF ...
            if (transformer instanceof CGTransformerConfigurable configurable) {
                Object cfgvalues = cfgmap.get("config");
                if (cfgvalues != null) {
                    configurable.accept((cfgvalues));
                    logger.info(() -> "configured transformer with " + cfgvalues);
                }
            }

            transformers.add(transformer);
        }

        this.transformers.addAll(transformers);

    }

    public String process(String content) {
        for (CGTextTransformer transformer : transformers) {
            content = transformer.apply(content);
        }
        return content;
    }
}
