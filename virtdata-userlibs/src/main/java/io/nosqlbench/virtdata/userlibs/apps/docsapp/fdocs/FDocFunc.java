package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.processors.DocCtorData;
import io.nosqlbench.virtdata.processors.DocFuncData;

import java.util.*;
import java.util.stream.Collectors;

public class FDocFunc implements Comparable<FDocFunc> {

    private final String funcName;
    private final Set<Category> categories;
    private String className;
    private String classJavaDoc;
    private String packageName;
    private List<FDocCtor> ctors;
    private String inType;
    private String outType;

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
