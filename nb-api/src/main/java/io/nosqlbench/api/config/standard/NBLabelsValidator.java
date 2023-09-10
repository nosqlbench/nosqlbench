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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class NBLabelsValidator implements Function<NBLabels, NBLabels> {
    private final static Logger logger = LogManager.getLogger(NBLabelsValidator.class);

    private final String config;
    private final List<String> requiredFields = new ArrayList<>();
    private final List<String> disallowedFields = new ArrayList<>();

    public NBLabelsValidator(String config) {
        this.config = config;
        for (String component : config.split("[ ;,]")) {
            if (component.startsWith("-")) disallowedFields.add(component.substring(1));
            if (component.startsWith("+")) requiredFields.add(component.substring(1));
        }
    }

    @Override
    public NBLabels apply(NBLabels labels) {
        Set<String> keyset = labels.asMap().keySet();
        LinkedList<String> missingFields = new LinkedList<>(requiredFields);
        LinkedList<String> extraneousFields = new LinkedList<>(disallowedFields);
        missingFields.removeIf(keyset::contains);
        extraneousFields.removeIf(extra -> !keyset.contains(extra));

        Result result = new Result(config, missingFields, extraneousFields);
        if (!result.isError()) {
            return labels;
        }
        logger.warn(result);
        throw new RuntimeException(result.toString());
    }


    record Result(String config, LinkedList<String> missingFields, LinkedList<String> extraneousFields) {
        public boolean isError() {
            return !missingFields.isEmpty() || !extraneousFields.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder err = new StringBuilder();
            if (!missingFields.isEmpty()) {
                err.append("The label set is missing required label names: ").append(missingFields).append("\n");
            }
            if (!extraneousFields.isEmpty()) {
                err.append("The label set has disallowed label names: ").append(extraneousFields).append("\n");
            }
            if (!err.isEmpty()) {
                err.append("This is controlled by the labeling policy: '").append(config).append("'\n");
            }
            return err.toString();
        }
    }
}
