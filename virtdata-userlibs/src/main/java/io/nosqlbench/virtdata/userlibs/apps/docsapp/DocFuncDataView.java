package io.nosqlbench.virtdata.userlibs.apps.docsapp;

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
