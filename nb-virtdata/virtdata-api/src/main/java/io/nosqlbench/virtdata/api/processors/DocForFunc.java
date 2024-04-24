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

package io.nosqlbench.virtdata.api.processors;

import io.nosqlbench.virtdata.api.annotations.Category;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DocForFunc implements DocFuncData {

    private String packageName;
    private String className;
    private String classJavadoc;
    private String inType;
    private String outType;
    private final ArrayList<DocCtorData> ctors = new ArrayList<>();
    private Category[] categories = new Category[]{};

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public Category[] getCategories() {
        return categories;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassJavadoc(String classJavadoc) {
        this.classJavadoc = classJavadoc;
    }

    @Override
    public String getClassJavadoc() {
        return classJavadoc;
    }

    public void setInType(String inType) {
        this.inType = inType;
    }

    @Override
    public String getInType() {
        return inType;
    }

    public void setOutType(String outType) {
        this.outType = outType;
    }

    @Override
    public String getOutType() {
        return outType;
    }

    public void addCtor(String ctorDoc, LinkedHashMap<String, String> args, List<List<String>> examples) {
        if (this.className == null || this.className.isEmpty()) {
            throw new RuntimeException("Unable to document ctor without known class name first.");
        }
        DocForFuncCtor ctor = new DocForFuncCtor(getClassName(), ctorDoc, args, examples);
        ctors.add(ctor);
    }

    @Override
    public ArrayList<DocCtorData> getCtors() {
        return this.ctors;
    }

    @Override
    public String toString() {
        return "DocForFunction{" +
            "(" + className + ")" +
            "packageName='" + packageName + '\'' +
            ", className='" + className + '\'' +
            ", classJavadoc='" + classJavadoc + '\'' +
            ", inType='" + inType + '\'' +
            ", outType='" + outType + '\'' +
            ", ctors=" + ctors +
            '}';
    }

    public void addCategories(Category[] categories) {
        this.categories = categories;
    }


}
