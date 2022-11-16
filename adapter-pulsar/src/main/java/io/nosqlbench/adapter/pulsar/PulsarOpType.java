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

package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum PulsarOpType {
    AdminTenant("admin-tenant"),
    AdminNamespace("admin-namespace"),
    AdminTopic("admin-topic"),
    MessageProduce("msg-send"),
    // This also supports multi-topic message consumption
    MessageConsume("msg-consume"),
    MessageRead("msg-read");

    public final String label;

    PulsarOpType(String label) {
        this.label = label;
    }


    public static boolean isValidPulsarOpType(String type) {
        return Arrays.stream(values())
            .anyMatch(t -> t.label.equalsIgnoreCase(type));
    }

    public static String getValidPulsarOpTypeList() {
        return Arrays.stream(values())
            .map(t -> t.label)
            .collect(Collectors.joining(", "));
    }
}


