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

package io.nosqlbench.nb.api.labels;

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.LinkedHashMap;
import java.util.Map;

public class NBLabelSpec {

    public static NBLabels parseLabels(String labeldata) {
        NBLabels buf = NBLabels.forKV();

        Map<String, String> parsedLabels = new LinkedHashMap<>();
        for (String component : labeldata.split("[,; ]+")) {
            String[] parts = component.split("[:=]+", 2);
            if (parts.length != 2) {
                throw new BasicError("Unable to parse labels to set:" + labeldata);
            }
            if (parts[1].startsWith("#")) {
                buf = buf.and(NBLabels.forKV().and(parts[0], parts[1].substring(1)));
            } else if (parts[1].startsWith("$")) {
                buf = buf.and(NBLabels.forKV().and(parts[0], parts[1].substring(1)));
            } else {
//                if (parts[1].matches(".*[0-9]+.*")) {
//                    throw new BasicError("You have specified an auxiliary tag which contains numbers in its value (" + component + "), but you have not designated it as a dimension, as in " + parts[0] + ":$" + parts[1] + " or an instance value, as in " + parts[0] + ":#" + parts[1]);
//                }
                buf = buf.and(NBLabels.forKV().and(parts[0], parts[1]));
            }
        }
        return buf;
    }

}
