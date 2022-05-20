package io.nosqlbench.virtdata.api.processors;

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


import io.nosqlbench.virtdata.api.annotations.ExampleData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocForFuncCtor implements DocCtorData {

    private Map<String, String> args = new LinkedHashMap<>();
    private String ctorDoc;
    private String className;
    private List<List<String>> examples = new ArrayList<>();

    public DocForFuncCtor(String className, String ctorDoc, Map<String, String> args, List<List<String>> examples) {
        this.className = className;
        this.ctorDoc = ctorDoc;
        this.args.putAll(args);
        ExampleData.validateExamples(examples);
        this.examples.addAll(examples);
    }


    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getCtorJavaDoc() {
        return ctorDoc;
    }

    @Override
    public String toString() {
        return "Ctor{" +
                "class=" + className +
                ", args=" + args +
                ", ctorDoc='" + ctorDoc + '\'' +
                '}';
    }

    @Override
    public Map<String, String> getArgs() {
        return args;
    }

    @Override
    public List<List<String>> getExamples() {
        return examples;
    }

}
