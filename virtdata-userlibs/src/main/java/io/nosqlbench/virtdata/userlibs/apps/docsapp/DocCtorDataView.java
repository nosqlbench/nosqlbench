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


import io.nosqlbench.virtdata.api.processors.DocCtorData;

import java.util.List;
import java.util.Map;

public class DocCtorDataView implements DocCtorData {
    private final DocCtorData dcd;

    public DocCtorDataView(DocCtorData dcd) {
        this.dcd = dcd;
    }

    @Override
    public String getClassName() {
        return dcd.getClassName();
    }

    @Override
    public String getCtorJavaDoc() {
        return dcd.getCtorJavaDoc();
    }

    @Override
    public Map<String, String> getArgs() {
        return dcd.getArgs();
    }

    @Override
    public List<List<String>> getExamples() {
        return dcd.getExamples();
    }
}
