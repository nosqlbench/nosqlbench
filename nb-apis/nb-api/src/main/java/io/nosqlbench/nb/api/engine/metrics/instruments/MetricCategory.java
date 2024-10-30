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

package io.nosqlbench.nb.api.engine.metrics.instruments;

public enum MetricCategory {
    /**
     * Metrics which are essential to understanding the behavior of any activity
     */
    Core,
    /**
     * Some metrics are provided only to inform the user of relative or absolute progress,
     * in terms of cycles remaining or similar
     */
    Progress,
    /**
     * Metrics which mirror configuration data, either static or dynamic during the lifetime
     * of an activity, session, or container. These are shared because they may need to be known
     * for further understanding or evaluation. For example, the target rate puts the achieved
     * rate in context, and having them both together in downstream metrics view makes computation
     * simple and direct.
     */
    Config,
    /**
     * Metrics which are used to ascertain the validity of client behavior, such as the CPU load
     * or other potentially contending effects. This is important because test results can be invalidated
     * when the composed system <PRE>{@code client <-> infrastructure <-> target system }</PRE> relies
     * too heavily on the testing apparatus. When the testing apparatus is under any degree of measurable
     * stress, the ability to drive the target system to its capacity is compromised, as well as the client's
     * ability to approximate real-time measurements due to task scheduling delays.
     */
    Internals,
    /**
     * When drivers are used to augment the metrics views, such as with the CQL client, these metrics can be
     * folded into metrics feeds. However, they are not part of the core NB metrics. Such auxiliary metric
     * need to be identified separately.
     */
    Driver,
    /**
     * Measurements of error rates, exception counts, and any other failure modes which can be counted or
     * otherwise quantified
     */
    Errors,
    /**
     * Verification logic is used to assert the validity or some property of results returned by individual
     * operations. Verification is meant to indicate whether a result was valid or invalid, with no
     * room for interpretation in between.
     */
    Verification,
    /**
     * Metrics which describe payload properties, such as result size or similar
     */
    Payload,
    /**
     * Sometimes users provide their own metrics instrumentation. These may or may not have descriptions provided.
     */
    User,
    /**
     * Some metrics help provide insight into analysis methods and progress. This can include parameters for
     * an optimizers configuration, parameters from a single frame of simulation, achieved results in the form of
     * a value function, or similar.
     */
    Analysis,
    /**
     * When the result returned by an operation is scrutinized for some degree of accuracy on a sliding scale,
     * this category applies. This is distinct from Verification in that verification implies a pass/fail scenario,
     * whereas accuracy measures are on a sliding scale where interpretation is often more subjective.
     */
    Accuracy
}
