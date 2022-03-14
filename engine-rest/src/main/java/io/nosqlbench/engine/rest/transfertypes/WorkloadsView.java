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

package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nosqlbench.engine.api.scenarios.WorkloadDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkloadsView {
    private final Map<String, List<WorkloadDesc>> workloadsByWorkspace = new HashMap<>();

    @JsonProperty("workloads")
    public Map<String,List<WorkloadDesc>> getWorkloads() {
        return workloadsByWorkspace;
    }

    public void add(String workspace, WorkloadDesc workload) {
        workloadsByWorkspace.computeIfAbsent(workspace, ws -> new ArrayList<>()).add(workload);
    }

    public void addAll(String workspace, List<WorkloadDesc> workloads) {
        workloadsByWorkspace.computeIfAbsent(workspace,ws -> new ArrayList<>()).addAll(workloads);
    }
}
