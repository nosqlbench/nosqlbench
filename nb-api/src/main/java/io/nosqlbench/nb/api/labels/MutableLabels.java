package io.nosqlbench.nb.api.labels;

import java.util.HashMap;
import java.util.Map;

public class MutableLabels extends HashMap<String,String> implements Labeled {

    public static MutableLabels fromMaps(Map<String,String> entries) {
        MutableLabels mutableLabels = new MutableLabels();
        mutableLabels.putAll(entries);
        return mutableLabels;
    }


    @Override
    public Map<String, String> getLabels() {
        return this;
    }
}
