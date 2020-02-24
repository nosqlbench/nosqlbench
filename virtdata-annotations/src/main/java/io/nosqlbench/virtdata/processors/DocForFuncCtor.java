package io.nosqlbench.virtdata.processors;

import io.nosqlbench.virtdata.annotations.ExampleData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocForFuncCtor implements DocCtorData {

    private Map<String, String> args = new LinkedHashMap<>();
    private String ctorDoc;
    private String className;
    private List<List<String>> examples = new ArrayList<>();

    public DocForFuncCtor(String className, String ctorDoc, Map<String, String> args, List<List<String>> examples) {
        this.className = className;
        this.ctorDoc = ctorDoc;
        this.args.putAll(args);
        ExampleData.validateExamples(examples);
        this.examples.addAll(examples);
    }


    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getCtorJavaDoc() {
        return ctorDoc;
    }

    @Override
    public String toString() {
        return "Ctor{" +
                "class=" + className +
                ", args=" + args +
                ", ctorDoc='" + ctorDoc + '\'' +
                '}';
    }

    @Override
    public Map<String, String> getArgs() {
        return args;
    }

    @Override
    public List<List<String>> getExamples() {
        return examples;
    }

}
