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

package io.nosqlbench.api.metadata;

/**
 * Where supported, the following named fields are injected into object which
 * implement this interface:
 * <UL>
 *     <LI>SCENARIO_NAME - The full scenario name, used for logging, metrics, etc</LI>
 *     <LI>STARTED_AT_MILLIS - The millisecond timestamp used to create the scenario name</LI>
 *     <LI>SYSTEM_ID - A stable identifier based on the available ip addresses</LI></LK>
 *     <LI>SYSTEM_FINGERPRINT - a stable and pseudonymous identifier based on SYSTEM_ID</LI>
 * </UL>
 */
public interface ScenarioMetadataAware {
    void setScenarioMetadata(ScenarioMetadata metadata);

    static void apply(Object target, ScenarioMetadata metadata) {
        if (target instanceof ScenarioMetadataAware) {
            ((ScenarioMetadataAware)target).setScenarioMetadata(metadata);
        }
    }
}
