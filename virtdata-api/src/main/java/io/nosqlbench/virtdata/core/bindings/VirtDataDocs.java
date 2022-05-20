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
                    docs.add((DocFuncData) o);
                } else {
                    throw new RuntimeException("class " + o.getClass() + " could not be assigned to " + DocFuncData.class.getSimpleName());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error while loading doc models:" + e);
        }
        return docs;

    }
}
