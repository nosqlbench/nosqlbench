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

package io.nosqlbench.nb.api.advisor;

import java.util.Locale;

/**
 * This is related to {@link io.nosqlbench.nb.api.advisor.conditions.Conditions}, and the terms
 * should be aligned. When possible, the the Conditions class should be used to capture
 * re-usable conditions, so that there is only one instance of each distinct type in the runtime,
 * regardless of how many components use it in their advisor points.
 *
 *
 */
public enum NBAdvisorLevel {
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

    // Static field to store the last used setting
    private static NBAdvisorLevel level = none;

    public static NBAdvisorLevel fromString(String advisorStr) {
        try {
            level = NBAdvisorLevel.valueOf(advisorStr.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            System.out.println("--advisor=" + advisorStr + " is invalid. Using 'none'");
            level = NBAdvisorLevel.none;
        }
        return level;
    }

    public static NBAdvisorLevel get() {
        return level;
    }

    public static boolean isAdvisorActive() {
        return level != NBAdvisorLevel.none;
    }

    public static boolean isEnforcerActive() {
        return level == NBAdvisorLevel.enforce;
    }

}
