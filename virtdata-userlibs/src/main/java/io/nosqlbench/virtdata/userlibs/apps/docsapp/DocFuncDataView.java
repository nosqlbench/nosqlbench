package io.nosqlbench.virtdata.userlibs.apps.docsapp;

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


import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.processors.DocFuncData;

import java.util.List;
import java.util.stream.Collectors;

public class DocFuncDataView {

    private final DocFuncData dfd;

    public DocFuncDataView(DocFuncData dfd) {
        this.dfd = dfd;
    }

    public String getPackageName() {
        return dfd.getPackageName();
    }

    public Category[] getCategories() {
        return dfd.getCategories();
    }

    public String getClassName() {
        return dfd.getClassName();
    }

    public String getClassJavadoc() {
        return dfd.getClassJavadoc();
    }

    public String getInType() {
        return dfd.getInType();
    }

    public String getOutType() {
        return dfd.getOutType();
    }

    public List<DocCtorDataView> getCtors() {
        return dfd.getCtors().stream()
            .map(DocCtorDataView::new)
            .collect(Collectors.toList());
    }
}
