/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.processors.DocCtorData;
import io.nosqlbench.virtdata.api.processors.DocFuncData;

import java.util.*;
import java.util.stream.Collectors;

public class FDocFunc implements Comparable<FDocFunc> {

    private final String funcName;
    private final Set<Category> categories;
    private final String className;
    private final String classJavaDoc;
    private final String packageName;
    private final List<FDocCtor> ctors;
    private final String inType;
    private final String outType;

    public FDocFunc(DocFuncData docFuncData) {
        this.funcName = docFuncData.getClassName();
        this.categories = new HashSet<>(Arrays.asList(docFuncData.getCategories()));
        this.className = docFuncData.getClassName();
        this.classJavaDoc= docFuncData.getClassJavadoc();
        this.packageName=docFuncData.getPackageName();
        this.inType=docFuncData.getInType();
        this.outType=docFuncData.getOutType();
        this.ctors=docFuncData.getCtors().stream().map(f -> new FDocCtor(f,inType,outType)).collect(Collectors.toList());
    }

    public String getClassName() {
        return className;
    }

    public String getClassJavaDoc() {
        return classJavaDoc;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getInType() {
        return inType;
    }

    public String getOutType() {
        return outType;
    }

    public String getFuncName() {
        return funcName;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    @Override
    public int compareTo(FDocFunc o) {
        int result = this.className.compareTo(o.className);
        if (result!=0) return result;
        result = this.getPackageName().compareTo(o.getPackageName());
        return result;
    }

    public List<FDocCtor> getCtors() {
        return ctors;
    }

    public CharSequence asMarkdown() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
