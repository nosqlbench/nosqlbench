package io.nosqlbench.activitytype.cql.filtering;

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


import io.nosqlbench.activitytype.cql.errorhandling.CQLExceptionEnum;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultFilterDispenser;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultValueFilterType;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.EnumReadableMappingFilter;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.TristateFilter;
import io.nosqlbench.engine.api.util.ConfigTuples;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Predicate;

@Service(value = ResultValueFilterType.class, selector = "cql")
public class CQLResultFilterType implements ResultValueFilterType {

    @Override
    public ResultFilterDispenser getDispenser(String config) {
        return new Dispenser(config);
    }

    private class Dispenser implements ResultFilterDispenser {
        private final ConfigTuples conf;
        private final EnumReadableMappingFilter<CQLExceptionEnum> enumFilter;
        private final Predicate<ResultReadable> filter;

        public Dispenser(String config) {
            this.conf = new ConfigTuples(config);
            ConfigTuples inout = conf.getAllMatching("in.*", "ex.*");

            // Default policy is opposite of leading rule
            TristateFilter.Policy defaultPolicy = TristateFilter.Policy.Discard;
            if (conf.get(0).get(0).startsWith("ex")) {
                defaultPolicy = TristateFilter.Policy.Keep;
            }

            this.enumFilter =
                    new EnumReadableMappingFilter<>(CQLExceptionEnum.values(), TristateFilter.Policy.Ignore);

            for (ConfigTuples.Section section : inout) {
                if (section.get(0).startsWith("in")) {
                    this.enumFilter.addPolicy(section.get(1), TristateFilter.Policy.Keep);
                } else if (section.get(0).startsWith("ex")) {
                    this.enumFilter.addPolicy(section.get(1), TristateFilter.Policy.Discard);
                } else {
                    throw new RuntimeException("Section must start with in(clude) or ex(clude), but instead it is " + section);
                }

            }

            this.filter = this.enumFilter.toDefaultingPredicate(defaultPolicy);
        }

        @Override
        public Predicate<ResultReadable> getResultFilter() {
            return filter;
        }
    }

}
