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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.ResultFilteringSieve;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.TristateFilter;
import io.nosqlbench.engine.api.util.ConfigTuples;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Predicate;

/**
 * This cycle result filter implements a filter that allows for the
 * inclusion or exclusion of single-values or intervals. It parses a format
 * that looks like this:
 *
 * <pre>include:54,exclude:32-35,...</pre>
 *
 * The default terminal policy -- the one that is applied if none of the
 * clauses match a given item -- is set as the opposite of the first
 * clause. In the example above, the default policy would be "exclude",
 * given that the fist clause is "include".
 */
@Service(value = ResultValueFilterType.class, selector = "core")
public class CoreResultValueFilter implements ResultValueFilterType {

    @Override
    public ResultFilterDispenser getDispenser(String config) {
        return new Dispenser(config);
    }

    public static class Dispenser implements ResultFilterDispenser {
        Predicate<ResultReadable> predicate;

        public Dispenser(String config) {
            ConfigTuples conf = new ConfigTuples(config);
            ConfigTuples includesAndExcludes = conf.getAllMatching("in.*", "ex.*");
            ResultFilteringSieve.Builder builder = new ResultFilteringSieve.Builder();
            includesAndExcludes.forEach(s -> mapPredicate(s,builder));
            ResultFilteringSieve sieve = builder.build();
            predicate = sieve.toDefaultingPredicate(getDefaultFromHead(includesAndExcludes.get(0)));
        }

        @Override
        public Predicate<ResultReadable> getResultFilter() {
            return predicate;
        }
    }

    private static TristateFilter.Policy getDefaultFromHead(ConfigTuples.Section section) {
        if (section.get(0).startsWith("in")) return TristateFilter.Policy.Discard;
        return TristateFilter.Policy.Keep;
    }

    private static void mapPredicate(ConfigTuples.Section section, ResultFilteringSieve.Builder builder) {
        int min, max;
        String incexc = section.get(0);

        if (incexc.startsWith("in")) {
            incexc = "include";
        } else if (incexc.startsWith("ex")) {
            incexc = "exclude";
        } else {
            throw new RuntimeException("pattern does not start with 'in' or 'ex' for include or exclude:" + incexc);
        }

        if (section.get(1).matches("\\d+-\\d+")) {
            String[] split = section.get(1).split("-");
            min = Integer.valueOf(split[0]);
            max = Integer.valueOf(split[1]);
        } else {
            min = Integer.valueOf(section.get(1));
            max = min;
        }

        if (min == max) {
            if (incexc.equals("include")) builder.include(min);
            else builder.exclude(min);
        } else {
            if (incexc.equals("include")) builder.include(min,max);
            else builder.exclude(min,max);
        }

    }

}
