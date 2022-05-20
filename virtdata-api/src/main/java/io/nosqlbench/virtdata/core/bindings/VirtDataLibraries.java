package io.nosqlbench.virtdata.core.bindings;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtDataLibraries implements VirtDataFunctionLibrary  {
    private final static Logger logger = LogManager.getLogger(VirtDataLibraries.class);
    private static final VirtDataLibraries instance = new VirtDataLibraries();
    private final Map<String, DataMapper<?>> threadSafeCache = new HashMap<>();

    private final VirtDataFunctionResolver resolver = new VirtDataFunctionResolver();

    public static VirtDataLibraries get() {
        return instance;
    }

    private VirtDataLibraries() {
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
