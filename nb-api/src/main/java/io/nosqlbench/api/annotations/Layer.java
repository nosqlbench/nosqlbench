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

package io.nosqlbench.api.annotations;

public enum Layer {

    /**
     * Events which describe command line arguments, such as parsing,
     * named scenario mapping, or critical errors
     */
    CLI,

    /**
     * Events which describe scenario execution, such as parameters,
     * lifecycle events, interruptions, and critical errors
     */
    Scenario,

    /**
     * Events which describe scripting details, such as commands,
     * extension usages, sending programmatic annotations, or critical errors
     */
    Script,

    /**
     * Events which are associated with a particular activity instance,
     * such as parameters, starting and stopping, and critical errors
     */
    Activity,

    /**
     * Events which are associated with a particular activity thread
     */
    Motor,

    /**
     * Events which are associated with a particular operation or op template
     */
    Operation
}
