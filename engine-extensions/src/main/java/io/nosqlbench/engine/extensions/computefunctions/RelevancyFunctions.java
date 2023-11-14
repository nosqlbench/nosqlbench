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

package io.nosqlbench.engine.extensions.computefunctions;

import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.engine.extensions.computefunctions.relavency.*;

import java.util.Map;

public class RelevancyFunctions {
    public static Recall recall(String name, int k, Map<String, String> labels) {
        return new Recall(name, k, labels);
    }

    public static Recall recall(String name, int k, Object... labels) {
        return new Recall(name, k, labels);
    }

    public static Recall recall(String name, int k) {
        return new Recall(name, k);
    }

    public static Precision precision(String name, int k, Map<String, String> labels) {
        return new Precision(name, k, labels);
    }

    public static Precision precision(String name, int k, Object... labels) {
        return new Precision(name, k, labels);
    }

    public static Precision precision(String name, int k) {
        return new Precision(name, k);
    }

    public static F1 F1(String name, int k, Map<String, String> labels) {
        return new F1(name, k, labels);
    }

    public static F1 F1(String name, int k, Object... labels) {
        return new F1(name, k, labels);
    }

    public static F1 F1(String name, int k) {
        return new F1(name, k);
    }

    public static AveragePrecision average_precision(String name, int k, Map<String, String> labels) {
        return new AveragePrecision(name, k, labels);
    }

    public static AveragePrecision average_precision(String name, int k, Object... labels) {
        return new AveragePrecision(name, k, labels);
    }

    public static AveragePrecision average_precision(String name, int k) {
        return new AveragePrecision(name, k);
    }

    public static ReciprocalRank reciprocal_rank(String name, int k, Map<String, String> labels) {
        return new ReciprocalRank(name, k, labels);
    }

    public static ReciprocalRank reciprocal_rank(String name, int k, Object... labels) {
        return new ReciprocalRank(name, k, labels);
    }

    public static ReciprocalRank rank_reciprocal(String name, int k) {
        return new ReciprocalRank(name, k, NBLabels.forKV("k", k));
    }

}
