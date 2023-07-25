/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;

import java.util.Arrays;

public class ResultMappingArrayFilter implements TristateFilter<ResultReadable> {

    private Policy[] policyResultMap = new Policy[0];

    public void addPolicy(ResultReadable readable, Policy defaultPolicy) {
        int result = readable.getResult();
        if (policyResultMap.length < result + 1) {
            policyResultMap = Arrays.copyOf(policyResultMap, result + 1);
        }
        policyResultMap[result] = defaultPolicy;
    }

    @Override
    public Policy apply(ResultReadable readable) {
        int result = readable.getResult();
        if (result > policyResultMap.length + 1) {
            throw new RuntimeException(
                    "Looking up a cycleResult of " + result +
                            " is not possible with a map array length of " + policyResultMap.length);
        }
        return policyResultMap[result];
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < policyResultMap.length; i++) {
            sb.append(i).append("->").append(policyResultMap[i]).append("\n");
        }
        return sb.toString();

    }

    public Policy getPolicy(int result) {
        return this.policyResultMap[result];
    }

}
