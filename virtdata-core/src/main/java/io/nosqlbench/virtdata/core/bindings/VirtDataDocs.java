package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.api.processors.DocFuncData;
import io.nosqlbench.virtdata.api.processors.FunctionDocInfoProcessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the top-level API supporting access to the documentation models
 * for all known {@link io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper} and
 * {@link io.nosqlbench.virtdata.api.annotations.PerThreadMapper} instances in the runtime.
 */
public class VirtDataDocs {

    private final static MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    public static List<String> getAllNames() {
        VirtDataFunctionFinder finder = new VirtDataFunctionFinder();
        return finder.getFunctionNames();
    }

    public static List<DocFuncData> getAllDocs() {
        VirtDataFunctionFinder finder = new VirtDataFunctionFinder();
        List<String> functionNames = finder.getFunctionNames();
        List<DocFuncData> docs = new ArrayList<>();
        try {
            for (String n : functionNames) {
                String s = n + FunctionDocInfoProcessor.AUTOSUFFIX;
                Class<?> aClass = Class.forName(s);
                MethodHandle constructor = lookup.findConstructor(aClass, MethodType.methodType(Void.TYPE));
                Object o = constructor.invoke();
                if (DocFuncData.class.isAssignableFrom(o.getClass())) {
                    docs.add(DocFuncData.class.cast(o));
                } else {
                    throw new RuntimeException("class " + o.getClass() + " could not be assigned to " + DocFuncData.class.getSimpleName());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error while loading doc models:" + e.toString());
        }
        return docs;

    }
}
