/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.api.config.standard;

import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.filtering.FilteringSieve;
import io.nosqlbench.api.filtering.TristateFilter;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class NBLabelsFilter implements Function<NBLabels, NBLabels> {

    private final FilteringSieve<String> filter;

    public NBLabelsFilter(String config) {
        FilteringSieve.Builder<String> filterBuilder = new FilteringSieve.Builder<>();
        for (String component : config.split("[ ;,]")) {
            TristateFilter.Policy policyForStage = TristateFilter.Policy.Keep;
            if (component.startsWith("-")) {
                component = component.substring(1);
                policyForStage = TristateFilter.Policy.Discard;
            } else if (component.startsWith("+")) {
                component = component.substring(1);
            }
            Pattern pattern = Pattern.compile(component);
            filterBuilder = filterBuilder.withPhase(s -> pattern.matcher(s).matches(), policyForStage);
        }
        filterBuilder=filterBuilder.keepByDefault();
        this.filter = filterBuilder.build();
    }

    @Override
    public NBLabels apply(NBLabels labels) {
        Map<String, String> filteredMap = filter.filterMapKeys(labels.asMap());
        return NBLabels.forMap(filteredMap);
    }
}
