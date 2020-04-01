package io.nosqlbench.virtdata.api;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtDataLibraries implements VirtDataFunctionLibrary  {
    private final static Logger logger  = LogManager.getLogger(VirtDataLibraries.class);private static VirtDataLibraries instance = new VirtDataLibraries();
    private final Map<String,DataMapper<?>> threadSafeCache = new HashMap<>();

    private final VirtDataFunctionResolver resolver = new VirtDataFunctionResolver();

    public static VirtDataLibraries get() {
        return instance;
    }

    private VirtDataLibraries() {
    }
    @Override

    public String getName() {
        return "ALL";
    }

    @Override
    public List<ResolvedFunction> resolveFunctions(
            Class<?> returnType,
            Class<?> inputType,
            String functionName,
            Map<String,?> customConfig,
            Object... parameters)
    {
        List<ResolvedFunction> resolvedFunctions = new ArrayList<>();


        List<ResolvedFunction> resolved = resolver.resolveFunctions(returnType, inputType, functionName, customConfig, parameters);
        // Written this way to allow for easy debugging and understanding, do not convert to .stream()...
        if (resolved.size()>0) {
            resolvedFunctions.addAll(resolved);
        }
        return resolvedFunctions;
    }
}
