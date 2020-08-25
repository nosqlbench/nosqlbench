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
