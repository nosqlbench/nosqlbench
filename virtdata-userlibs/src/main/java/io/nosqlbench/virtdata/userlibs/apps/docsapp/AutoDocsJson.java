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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.api.processors.DocFuncData;

import java.util.List;

public class AutoDocsJson {

//    public static String getAsJson() {
//        List<DocFuncData> docModels = VirtDataDocs.getAllDocs();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(docModels);
//        return json;
//    }

    public static String getAsJson(List<DocFuncData> groupedDocs) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(groupedDocs);
        return json;

    }
}
