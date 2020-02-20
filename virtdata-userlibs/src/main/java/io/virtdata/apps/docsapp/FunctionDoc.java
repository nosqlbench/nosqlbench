package io.virtdata.apps.docsapp;

import io.virtdata.annotations.Category;
import io.virtdata.api.config.MutableConfigModel;
import io.virtdata.processors.DocCtorData;

import java.util.*;

public class FunctionDoc {

    private String funcName;
    private String classDocs;
    private Set<Category> categories= new HashSet<>();
    private List<DocCtorData> ctors = new ArrayList<>();

    public FunctionDoc(String funcName) {
        this.funcName = funcName;
    }

    public void setClassDocs(String distinctClassDocs) {
        this.classDocs = distinctClassDocs;
    }

    public void addCategories(Category[] categories) {
        this.categories.addAll(Arrays.asList(categories));
    }

    public void addCtor(DocCtorData ctor) {
        this.ctors.add(ctor);
    }
}
