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

package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBMapConfigurable;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Map;
import java.util.function.Function;

/**
 * Load a value from a map, based on the injected configuration.
 * The map which is used must be named by the mapname.
 * If the injected configuration contains a variable of this name
 * which is also a Map, then this map is referenced and read
 * by the provided variable name.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class LoadElement implements Function<Object,Object>, NBMapConfigurable {

    private final String varname;
    private final Object defaultValue;
    private final String mapname;

    private Map<String,?> vars;

    @Example({"LoadElement('varname','vars','defaultvalue')","Load the varable 'varname' from a map named 'vars', or provide 'defaultvalue' if neither is provided"})
    public LoadElement(String varname, String mapname, Object defaultValue) {
        this.mapname = mapname;
        this.varname = varname;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (vars==null) {
            return defaultValue;
        }
        Object object = vars.get(varname);
        return (object!=null) ? object : defaultValue;
    }

    @Override
    public void applyConfig(Map<String, ?> providedConfig) {
        Map<String, ?> vars = (Map<String, ?>) providedConfig.get(mapname);
        if (vars != null) {
            this.vars = vars;
        }
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass(), Param.optional("<mapname>", Map.class));
    }
}
