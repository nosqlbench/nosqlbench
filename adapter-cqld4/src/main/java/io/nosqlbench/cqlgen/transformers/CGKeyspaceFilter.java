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

import io.nosqlbench.cqlgen.api.CGModelTransformer;
import io.nosqlbench.cqlgen.api.CGTransformerConfigurable;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlKeyspaceDef;
import io.nosqlbench.cqlgen.model.CqlModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CGKeyspaceFilter implements CGModelTransformer, CGTransformerConfigurable {

    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/keyspace-filter");
    private List<TriStateFilter> patterns;
    private String name;

    @Override
    public String getName() {
        return this.name;
    }

    private enum InclExcl {
        include,
        exclude
    }

    private enum Action {
        add,
        remove,
        inderminate
    }

    @Override
    public CqlModel apply(CqlModel model) {
        List<String> ksnames = model.getKeyspaceDefs().stream().map(CqlKeyspaceDef::getName).toList();
        for (String keyspace : ksnames) {
            Action action = Action.inderminate;
            for (TriStateFilter pattern : patterns) {
                action = pattern.apply(keyspace);
                switch (action) {
                    case add:
                        logger.debug(() -> "including all definitions in " + keyspace + " with inclusion pattern " + pattern);
                        break;
                    case remove:
                        logger.info(() -> "removing all definitions in " + keyspace + " with exclusion pattern " + pattern);
                        model.removeKeyspaceDef(keyspace);
                    case inderminate:
                }
            }
            if (action == Action.inderminate) {
                logger.warn("Undetermined status of keyspace filter. No includes or excludes matched, and no default pattern was at the end of the list. Consider adding either include: '.*' or exclude: '.*' at the end.");
            }

        }

        return model;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private static class TriStateFilter implements Function<String, Action> {
        private final InclExcl filterType;
        private final Pattern pattern;

        public TriStateFilter(String filterType, String pattern) {
            this(InclExcl.valueOf(filterType), Pattern.compile(pattern));
        }

        public TriStateFilter(InclExcl filterType, Pattern pattern) {
            this.filterType = filterType;
            this.pattern = pattern;
        }

        public TriStateFilter(Map.Entry<String, String> entry) {
            this(entry.getKey(), entry.getValue());
        }

        @Override
        public Action apply(String s) {
            return switch (filterType) {
                case exclude -> pattern.matcher(s).matches() ? Action.remove : Action.inderminate;
                case include -> pattern.matcher(s).matches() ? Action.add : Action.inderminate;
            };
        }

        public String toString() {
            return filterType + ": " + pattern.pattern();
        }
    }

    @Override
    public void accept(Object cfgobj) {
        if (cfgobj instanceof List cfglist) {
            List<Map<String, String>> filters = (List<Map<String, String>>) cfglist;
            if (filters != null) {
                this.patterns = filters.stream()
                    .map(m -> {
                            if (m.size() != 1) {
                                throw new RuntimeException("Each filter entry must be a single keyed map with include or exclude keys, and a regex value.");
                            }
                            return new ArrayList<>(m.entrySet()).get(0);
                        }
                    ).map(TriStateFilter::new)
                    .toList();
            }
        } else {
            throw new RuntimeException("keyspace filter requires a Map object for it's config value, full of single key maps as (include|exclude): regex");
        }
    }


}
