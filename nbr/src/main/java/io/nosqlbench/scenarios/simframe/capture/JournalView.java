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

import io.nosqlbench.scenarios.simframe.planning.SimFrame;

import java.util.List;

/**
 * Provide an observer-only view of a simulation journal
 */
public interface JournalView<P> {
    List<SimFrame<P>> frames();
    SimFrame<P> last();
    SimFrame<P> beforeLast();
    SimFrame<P> bestRun();
    SimFrame<P> before(SimFrame<P> frame);
    SimFrame<P> after(SimFrame<P> frame);
    JournalView<P> reset();
}
