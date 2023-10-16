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

package io.nosqlbench.engine.api.activityapi.ratelimits.simrate;

import io.nosqlbench.api.engine.activityimpl.ParameterMap;

public class StrideRateSpec extends SimRateSpec {
    public StrideRateSpec(double opsPerSec, double burstRatio) {
        super(opsPerSec, burstRatio);
    }

    public StrideRateSpec(double opsPerSec, double burstRatio, Verb type) {
        super(opsPerSec, burstRatio, type);
    }

    public StrideRateSpec(ParameterMap.NamedParameter tuple) {
        super(tuple);
    }

    public StrideRateSpec(String spec) {
        super(spec);
    }
}
