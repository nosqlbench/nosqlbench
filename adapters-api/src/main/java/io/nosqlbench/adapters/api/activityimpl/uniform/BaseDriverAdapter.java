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

package io.nosqlbench.adapters.api.activityimpl.uniform;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.adapters.api.activityimpl.uniform.fieldmappers.FieldDestructuringMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public abstract class BaseDriverAdapter<R extends Op, S> implements DriverAdapter<R, S>, NBConfigurable, NBReconfigurable {
    private final static Logger logger = LogManager.getLogger("ADAPTER");

    private DriverSpaceCache<? extends S> spaceCache;
    private NBConfiguration cfg;
    private LongFunction<S> spaceF;

    /**
     * BaseDriverAdapter will take any provided functions from {@link #getOpStmtRemappers()}
     * and {@link #getOpFieldRemappers()} and construct a preprocessor list. These are applied
     * successively to the op template fields so long as remapping occurs.
     *
     * @return a list of preprocessors for op template fields
     */
    @Override
    public final Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
        List<Function<Map<String, Object>, Map<String, Object>>> mappers = new ArrayList<>();
        List<Function<Map<String, Object>, Map<String, Object>>> stmtRemappers =
                getOpStmtRemappers().stream()
                        .map(m -> new FieldDestructuringMapper("stmt", m))
                        .collect(Collectors.toList());
        mappers.addAll(stmtRemappers);
        mappers.addAll(getOpFieldRemappers());

        if (mappers.size() == 0) {
            return (i) -> i;
        }

        Function<Map<String, Object>, Map<String, Object>> remapper = null;
        for (int i = 0; i < mappers.size(); i++) {
            if (i == 0) {
                remapper = mappers.get(i);
            } else {
                remapper = remapper.andThen(mappers.get(i));
            }
        }

        return remapper;
    }

    /**
     * <p>Provide a list of field remappers which operate exclusively on the 'stmt' field
     * in the op template. These are useful, for example, for taking the 'stmt' field
     * and parsing it into component fields which might otherwise be specified by the user.
     * This allows users to specify String-form op templates which are automatically
     * parsed and destructured into the canonical field-wise form for a given type of
     * operation.</p>
     * <p>
     * <br/>
     *
     * <p>Each function in this list is applied in order. If the function returns a value,
     * then the 'stmt' field is removed and the resulting map is added to the other
     * fields in the op template.</p>
     * <p>
     * <br/>
     *
     * <p>If a driver adapter is meant to support the {@code stmt} field, then this
     * <em>must</em> be provided. The use of the stmt field should be documented in
     * the driver adapter user docs with examples for any supported formats. A default
     * implementation does nothing, as it must be decided per-driver whether or not
     * the stmt field will be used directly or whether it is short-hand for a more
     * canonical form.
     * <p>
     * <br/>
     *
     * <p>If you want to automatically destructure stmt values into a map and inject
     * its entries into the op template fields, simply provide an implementation
     * like this:<pre>
     * {@code
     *     return List.of(stmt -> Optional.of(NBParams.one(stmt).getMap()));
     * }
     * </pre></p>
     *
     * @return A list of optionally applied remapping functions.
     */
    public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
        return List.of();
    }

    /**
     * <p>Provide a list of field remappers which operate on arbitrary fields.
     * Each function is applied to the op template fields. </p>
     *
     * @return
     */
    @Override
    public List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
        return List.of();
    }

    @Override
    public final synchronized DriverSpaceCache<? extends S> getSpaceCache() {
        if (spaceCache == null) {
            spaceCache = new DriverSpaceCache<>(getSpaceInitializer(getConfiguration()));
        }
        return spaceCache;
    }

    @Override
    public NBConfiguration getConfiguration() {
        return cfg;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.cfg = cfg;
    }

    @Override
    public void applyReconfig(NBConfiguration reconf) {
        this.cfg = getReconfigModel().apply(reconf.getMap());
    }


    /**
     * In order to be provided with config information, it is required
     * that the driver adapter specify the valid configuration options,
     * their types, and so on.
     */
    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(BaseDriverAdapter.class)
                .add(Param.optional("alias"))
                .add(Param.defaultTo("strict", true, "strict op field mode, which requires that provided op fields are recognized and used"))
                .add(Param.optional(List.of("op", "stmt", "statement"), String.class, "op template in statement form"))
                .add(Param.optional("tags", String.class, "tags to be used to filter operations"))
                .add(Param.defaultTo("errors", "stop", "error handler configuration"))
                .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto").setDescription("number of concurrent operations, controlled by threadpool"))
                .add(Param.optional("stride").setRegex("\\d+"))
                .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
                .add(Param.optional("cycles").setRegex("\\d+[KMBGTPE]?|\\d+[KMBGTPE]?\\.\\.\\d+[KMBGTPE]?").setDescription("cycle interval to use"))
                .add(Param.optional("recycles").setDescription("allow cycles to be re-used this many times"))
                .add(Param.optional(List.of("cyclerate", "targetrate", "rate"), String.class, "rate limit for cycles per second"))
                .add(Param.optional("seq", String.class, "sequencing algorithm"))
                .add(Param.optional("instrument", Boolean.class))
                .add(Param.optional(List.of("workload", "yaml"), String.class, "location of workload yaml file"))
                .add(Param.optional("driver", String.class))
                .add(Param.defaultTo("dryrun", "none").setRegex("(op|jsonnet|none)"))
                .add(Param.optional("maxtries", Integer.class))
                .asReadOnly();
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return ConfigModel.of(BaseDriverAdapter.class)
                .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto").setDescription("number of concurrent operations, controlled by threadpool"))
                .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
                .add(Param.optional(List.of("cyclerate", "targetrate", "rate"), String.class, "rate limit for cycles per second"))
                .asReadOnly();
    }

    @Override
    public LongFunction<S> getSpaceFunc(ParsedOp pop) {
        LongFunction<String> spaceNameF = pop.getAsFunctionOr("space", "default");
        DriverSpaceCache<? extends S> cache = getSpaceCache();
        return l -> getSpaceCache().get(spaceNameF.apply(l));
    }
}
