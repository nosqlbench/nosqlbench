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

import io.nosqlbench.adapters.api.activityimpl.uniform.fieldmappers.FieldDestructuringMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.stream.Collectors;

/**
 Identity function for preprocessing maps. */
class IdentityPreprocessor implements Function<Map<String, Object>, Map<String, Object>> {
  @Override
  public Map<String, Object> apply(Map<String, Object> input) {
    return input;
  }
}

/**
 Function that converts cycle numbers to space names using a source function. */
class StringSpaceNameFunction implements LongFunction<String> {
  private final LongFunction<?> sourceFunction;

  public StringSpaceNameFunction(LongFunction<?> sourceFunction) {
    this.sourceFunction = sourceFunction;
  }

  @Override
  public String apply(long value) {
    return sourceFunction.apply(value).toString();
  }
}

/**
 Space function that maps cycle numbers to spaces using a string name function. */
class StringSpaceFunction<S extends Space> implements LongFunction<S> {
  private final ConcurrentStringSpaceCache<S> stringSpaceCache;
  private final LongFunction<String> nameFunction;

  public StringSpaceFunction(
      ConcurrentStringSpaceCache<S> stringSpaceCache,
      LongFunction<String> nameFunction
  )
  {
    this.stringSpaceCache = stringSpaceCache;
    this.nameFunction = nameFunction;
  }

  @Override
  public S apply(long value) {
    return stringSpaceCache.get(nameFunction.apply(value));
  }
}

public abstract class BaseDriverAdapter<RESULT extends CycleOp<?>, SPACE extends Space>
    extends NBBaseComponent
    implements DriverAdapter<RESULT, SPACE>, NBConfigurable, NBReconfigurable
{

  private final static Logger logger = LogManager.getLogger("ADAPTER");

  private ConcurrentStringSpaceCache<SPACE> spaceCache;
  private NBConfiguration cfg;
  private LongFunction<SPACE> spaceF;

  public BaseDriverAdapter(NBComponent parentComponent, NBLabels labels) {
    super(parentComponent, labels);
  }

  /**
   BaseDriverAdapter will take any provided functions from {@link #getOpStmtRemappers()}
   and {@link #getOpFieldRemappers()} and construct a preprocessor list. These are applied
   successively to the op template fields so long as remapping occurs.
   @return a list of preprocessors for op template fields
   */
  @Override
  public final Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
    List<Function<Map<String, Object>, Map<String, Object>>> mappers = new ArrayList<>();
    List<Function<Map<String, Object>, Map<String, Object>>> stmtRemappers =
        getOpStmtRemappers().stream().map(m -> new FieldDestructuringMapper("stmt", m))
            .collect(Collectors.toList());
    mappers.addAll(stmtRemappers);
    mappers.addAll(getOpFieldRemappers());

    if (mappers.isEmpty()) {
      return new IdentityPreprocessor();
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
   <p>Provide a list of field remappers which operate exclusively on the 'stmt' field
   in the op template. These are useful, for example, for taking the 'stmt' field
   and parsing it into component fields which might otherwise be specified by the user.
   This allows users to specify String-form op templates which are automatically
   parsed and destructured into the canonical field-wise form for a given type of
   operation.</p>
   <p>
   <br/>

   <p>Each function in this list is applied in order. If the function returns a value,
   then the 'stmt' field is removed and the resulting map is added to the other
   fields in the op template.</p>
   <p>
   <br/>

   <p>If a driver adapter is meant to support the {@code stmt} field, then this
   <em>must</em> be provided. The use of the stmt field should be documented in
   the driver adapter user docs with examples for any supported formats. A default
   implementation does nothing, as it must be decided per-driver whether or not
   the stmt field will be used directly or whether it is short-hand for a more
   canonical form.
   <p>
   <br/>

   <p>If you want to automatically destructure stmt values into a map and inject
   its entries into the op template fields, simply provide an implementation
   like this:<pre>
   {@code
   return List.of(stmt -> Optional.of(NBParams.one(stmt).getMap()));
   }
   </pre></p>
   @return A list of optionally applied remapping functions.
   */
  public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
    return List.of();
  }

  /**
   <p>Provide a list of field remappers which operate on arbitrary fields.
   Each function is applied to the op template fields. </p>
   @return op field remappers, an empty list by default
   */
  @Override
  public List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
    return List.of();
  }

  private final synchronized SpaceCache<SPACE> getSpaceCache() {
    if (spaceCache == null) {
      spaceCache = new ConcurrentStringSpaceCache<SPACE>(
          this,
          getSpaceInitializer(getConfiguration())
      );
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
   In order to be provided with config information, it is required
   that the driver adapter specify the valid configuration options,
   their types, and so on.
   */
  @Override
  public NBConfigModel getConfigModel() {
    return ConfigModel.of(BaseDriverAdapter.class).add(Param.optional("alias")).add(Param.optional(
            "labels",
            String.class,
            "Labels which will apply to metrics and annotations for this activity only"
        )).add(Param.defaultTo(
            "strict",
            true,
            "strict op field mode, which requires that provided op fields are recognized and used"
        )).add(Param.optional(
            List.of("op", "stmt", "statement"),
            String.class,
            "op template in statement form"
        )).add(Param.optional("tags", String.class, "tags to be used to filter operations"))
        .add(Param.defaultTo("errors", "stop", "error handler configuration"))
        .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto")
            .setDescription("number of concurrent operations, controlled by threadpool"))
        .add(Param.optional("stride").setRegex("\\d+"))
        .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
        .add(Param.optional("cycles").setRegex("\\d+[KMBGTPE]?|\\d+[KMBGTPE]?\\.\\.\\d+[KMBGTPE]?")
            .setDescription("cycle interval to use")).add(Param.optional("recycles")
            .setDescription("allow cycles to be re-used this many times")).add(Param.optional(
            List.of("cyclerate", "targetrate", "rate"),
            String.class,
            "rate limit for cycles per second"
        )).add(Param.optional("seq", String.class, "sequencing algorithm"))
        .add(Param.optional("instrument", Boolean.class))
        .add(Param.optional(
            List.of("workload", "yaml"),
            String.class,
            "location of workload yaml file"
        )).add(Param.optional("driver", String.class))
        .add(Param.defaultTo("dryrun", "none").setRegex("(op|jsonnet|emit|none)"))
        .add(Param.optional("maxtries", Integer.class)).asReadOnly();

  }

  @Override
  public NBConfigModel getReconfigModel() {
    return ConfigModel.of(BaseDriverAdapter.class)
        .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto")
            .setDescription("number of concurrent operations, controlled by threadpool"))
        .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
        .add(Param.optional(
            List.of("cyclerate", "targetrate", "rate"),
            String.class,
            "rate limit for cycles per second"
        )).asReadOnly();
  }

  /**
   Get the function that names space indexes for a given cycle value.
   This method extracts the naming function from the getSpaceFunc method.
   @param pop
   The parsed op
   @return A function that converts a cycle value to a name, or null if no naming function is available
   */
  public LongFunction<String> getSpaceNameFunc(ParsedOp pop) {
    Optional<LongFunction<Object>> spaceFuncTest = pop.getAsOptionalFunction("space", Object.class);
    if (spaceFuncTest.isEmpty()) {
      return null;
    }

    Object example = spaceFuncTest.get().apply(0L);
    if (example instanceof Number) {
      return null;
    } else {
      LongFunction<?> sourceF = pop.getAsRequiredFunction("space", String.class);
      final LongFunction<?> finalSourceF = sourceF;
      return new StringSpaceNameFunction(finalSourceF);
    }
  }

  /**
   Get the function that provides space instances for a given cycle value.
   This method constructs an appropriate lambda that does the following:
   <UL>
   <LI>Determines whether the function provided for "space" is present or not. If it is
   not, then the default one is used which simply returns a 0.</LI>
   <LI>If the type of the returned value is a Number, then the Number.toString() method is
   used to convert it to a string key for the ConcurrentStringSpaceCache.</LI>
   <LI>If the type is not a Number, then the toString() method is used to get a string key
   for the ConcurrentStringSpaceCache.</LI>
   </UL>
   @param pop
   The parsed operation
   @return A function that provides space instances for a given cycle value
   */
  @Override
  public LongFunction<SPACE> getSpaceFunc(ParsedOp pop) {
    LongFunction<String> spaceKeyFunction =
        pop.getAsOptionalFunction("space", String.class).orElse(DEFAULT_CYCLE_TO_SPACE_F);
    SpaceCache<SPACE> spaceCache1 = getSpaceCache();
    return new StringSpaceFunction<>(
        (ConcurrentStringSpaceCache<SPACE>) spaceCache1,
        spaceKeyFunction
    );
  }

  @Override
  public void beforeDetach() {
    // Close all spaces in the space cache
    if (spaceCache != null) {
      for (SPACE space : this.getSpaceCache()) {
        try {
          space.close();
        } catch (Exception e) {
          throw new RuntimeException(
              "Error while shutting down state space for " + "adapter=" + this.getAdapterName()
              + ", space=" + space.getName() + ": " + e,
              e
          );
        }
      }
    }

    super.beforeDetach();
  }

  /**
   The default cycle to space function used when no custom space mapper is provided.
   This is used to determine if a custom space mapper has been provided.
   */
  public static final LongFunction<String> DEFAULT_CYCLE_TO_SPACE_F = l -> "default";

}
