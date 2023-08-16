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

package io.nosqlbench.engine.api.util;

import io.nosqlbench.engine.api.activityapi.core.Activity;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleConfig {

    private final Map<String, String> params;

    public SimpleConfig(String configdata) {
        this.params = parseParams(configdata);
    }

    public SimpleConfig(Activity activity, String param) {
        this(activity.getParams().getOptionalString(param).orElse(""));
    }

    private Map<String, String> parseParams(String configdata) {
        try {
            return Arrays.stream(configdata.split("[,]"))
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                   // .peek(System.out::println)
                    .map(s -> s.split("[:=]"))
                    .collect(Collectors.toMap(o -> o[0], o -> o[1]));
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse params: '" + configdata + "': " + e);
        }
    }

    public Optional<String> getString(String name) {
        return Optional.ofNullable(params.get(name));
    }

    public Optional<Integer> getInteger(String name) {
        return Optional.ofNullable(params.get(name)).map(Integer::valueOf);
    }

    public Optional<Long> getLong(String name) {
        return Optional.ofNullable(params.get(name)).map(Long::valueOf);
    }

    public Optional<Double> getDouble(String name) {
        return Optional.ofNullable(params.get(name)).map(Double::valueOf);
    }

}
