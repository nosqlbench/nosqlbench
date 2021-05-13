package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBlock extends ArrayList<TestSet> {
    public TestBlock(TestSet... testsets) {
        this.addAll(Arrays.asList(testsets));
    }

    public void addTestSet(TestSet set) {
        this.add(set);
    }
}
