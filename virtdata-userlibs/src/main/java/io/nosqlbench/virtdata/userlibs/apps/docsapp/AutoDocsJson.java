package io.nosqlbench.virtdata.userlibs.apps.docsapp;

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
