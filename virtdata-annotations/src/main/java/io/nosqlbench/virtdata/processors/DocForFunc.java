package io.nosqlbench.virtdata.processors;

import io.nosqlbench.virtdata.annotations.Category;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DocForFunc implements DocFuncData {

    private String packageName;
    private String className;
    private String classJavadoc;
    private String inType;
    private String outType;
    private ArrayList<DocCtorData> ctors = new ArrayList<>();
    private Category[] categories = new Category[] { };

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
        if (this.className==null || this.className.isEmpty()) {
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
