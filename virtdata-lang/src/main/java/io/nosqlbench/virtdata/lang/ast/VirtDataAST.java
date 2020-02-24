package io.nosqlbench.virtdata.lang.ast;

import java.util.ArrayList;
import java.util.List;

public class VirtDataAST {
    private List<VirtDataFlow> flows = new ArrayList<>();

    public void addFlow(VirtDataFlow flow) {
        this.flows.add(flow);
    }

    public List<VirtDataFlow> getFlows() {
        return flows;
    }
}
