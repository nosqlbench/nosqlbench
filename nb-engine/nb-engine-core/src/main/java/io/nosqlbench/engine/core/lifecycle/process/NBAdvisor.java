/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.process;

import java.util.Locale;

public enum NBAdvisor {
    /**
     * Do not analyze arguments, scenarios, activities, and workloads
     */
    none,
    /**
     * Provide advice about invalid, incorrect, and unused ops
     */
    validate,
    /**
     * Only allow correct operations
     */
    enforce;

    public static NBAdvisor fromString(String advisorStr) {
        try {
            return NBAdvisor.valueOf(advisorStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            System.out.println("--advisor=" + advisorStr + " is invalid. Using 'none'");
            return NBAdvisor.none;
        }
    }
}