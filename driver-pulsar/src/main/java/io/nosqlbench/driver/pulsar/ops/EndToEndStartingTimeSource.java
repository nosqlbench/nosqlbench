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

package io.nosqlbench.driver.pulsar.ops;

public enum EndToEndStartingTimeSource {
    NONE, // no end-to-end latency calculation
    MESSAGE_PUBLISH_TIME, // use message publish timestamp
    MESSAGE_EVENT_TIME, // use message event timestamp
    MESSAGE_PROPERTY_E2E_STARTING_TIME // use message property called "e2e_starting_time" as the timestamp
}
