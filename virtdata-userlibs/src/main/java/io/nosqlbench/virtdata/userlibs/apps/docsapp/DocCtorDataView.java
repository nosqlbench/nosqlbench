package io.nosqlbench.virtdata.userlibs.apps.docsapp;

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
