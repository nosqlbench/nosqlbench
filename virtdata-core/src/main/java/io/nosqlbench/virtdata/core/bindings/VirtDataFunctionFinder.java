package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.api.processors.DocFuncData;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class VirtDataFunctionFinder {
    public VirtDataFunctionFinder() {
    }

    public List<String> getFunctionNames() {

        ServiceLoader<DocFuncData> loader =ServiceLoader.load(DocFuncData.class);
        List<String> names = new ArrayList<>();
        loader.iterator().forEachRemaining(d -> names.add(d.getPackageName() + "." + d.getClassName()));
        List<String> cleaned = names.stream().sorted().distinct().collect(Collectors.toList());
        return cleaned;
    }
}
