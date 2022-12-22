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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.api.config.standard.NBConfigurable;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CGModelTransformers implements
    Consumer<List<Map<String, ?>>>,
    Supplier<List<CGModelTransformer>>,
    Function<CqlModel,CqlModel> {

    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/transformers");
    private final List<CGModelTransformer> transformers = new ArrayList<>();

    public CGModelTransformers() {
    }

    @Override
    public void accept(List<Map<String, ?>> configs) {
        List<CGModelTransformer> transformers = new ArrayList<>();

        for (Map<String, ?> cfgmap : configs) {

            // Instantiate Transformer

            String classname = cfgmap.get("class").toString();
            String name = Optional.ofNullable(cfgmap.get("name")).orElseThrow().toString();

            if (!classname.contains(".")) {
                String newname = CGNameObfuscator.class.getPackageName() + "." + classname;
                logger.debug("qualified transformer '" + classname + "' as '" + newname + "'");
                classname = newname;
            }
            Class<?> txclass = null;
            CGModelTransformer transformer = null;
            try {
                txclass = Class.forName(classname);
                Constructor<?> ctor = txclass.getConstructor();
                Object instance = ctor.newInstance();
                if (instance instanceof CGModelTransformer t) {
                    transformer = t;
                    t.setName(name);
                } else {
                    throw new RuntimeException("Object " + instance.getClass().getName() + " is not a " + CGModelTransformer.class.getName());
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }

            if (transformer instanceof NBConfigurable nbc) {
                Object cfg = cfgmap.get("config");
                if (cfg instanceof Map tcfgmap) {
                    NBConfiguration configuration = nbc.getConfigModel().apply((Map<String, ?>) cfg);
                    nbc.applyConfig(configuration);
                } else {
                    throw new RuntimeException("config for " + nbc.getClass().getSimpleName() + " must be map.");
                }

            }
            // Configure Transformer IFF ...
            if (transformer instanceof CGTransformerConfigurable configurable) {
                Object cfgvalues = cfgmap.get("config");
                if (cfgvalues !=null ) {
                    configurable.accept((cfgvalues));
                    logger.info(() -> "configured transformer with " + cfgvalues);
                }
            }

            transformers.add(transformer);
        }

        this.transformers.addAll(transformers);

    }

    @Override
    public List<CGModelTransformer> get() {
        return this.transformers;
    }

    @Override
    public CqlModel apply(CqlModel cqlModel) {
        for (CGModelTransformer transformer : transformers) {
            cqlModel=transformer.apply(cqlModel);
        }
        return cqlModel;
    }
}
