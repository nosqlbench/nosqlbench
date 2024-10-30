/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.scenarios.simframe.capture;

public enum EvalType {
    /**
     * A basis value is read from a supplier directly at the end of a simulation frame with no additional computation.
     */
    direct,
    /**
     * At the beginning of a simulation frame, starting basis values and starting frame times are recorded. At the end
     * of that simulation frame, ending basis values and ending time are record. The basis value is computed as
     * ΔV/ΔT.
     */
    deltaT,
    /**
     * At the end of a simulation frame, any remix values are computed as a function over any previous defined basis values.
     */
    remix
}
