package io.nosqlbench.virtdata.processors;

import io.nosqlbench.virtdata.annotations.Category;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExampleDocData implements DocFuncData {

    @Override
    public String getPackageName() {
        return "packagename";
    }

    @Override
    public Category[] getCategories() {
        return new Category[] { Category.general };
    }

    @Override
    public String getClassName() {
        return "classname";
    }

    @Override
    public String getClassJavadoc() {
        return "javadoc";
    }

    @Override
    public String getInType() {
        return "intype";
    }

    @Override
    public String getOutType() {
        return "outtype";
    }

    @Override
    public List<DocCtorData> getCtors() {
        ArrayList<DocCtorData> ctors = new ArrayList<>();

        // for each ctor
        LinkedHashMap<String, String> args = new LinkedHashMap<>();
        args.put("arg1", "val1");
        List<List<String>> examples = new ArrayList<>();
        examples.add(new ArrayList<String>() {{ add("example"); add("one"); }});
        DocForFuncCtor ctordoc = new DocForFuncCtor("name", "ctordoc", args, examples);
        ctors.add(ctordoc);

        return ctors;
    }

    public List<DocForFuncCtor> getCtorsAlternate() {
        return new ArrayList<DocForFuncCtor>() {{
            add(new DocForFuncCtor("name", "ctordoc",
                    new LinkedHashMap<String, String>() {{
                        put("aname", "atype");
                    }},
                    new ArrayList<List<String>>() {{
                        add(new ArrayList<String>() {{
                            add("example");
                            add("description");
                        }});
                    }}
            ));
            add(new DocForFuncCtor("name", "ctordoc",
                    new LinkedHashMap<String, String>() {{
                        put("aname", "atype");
                    }},
                    new ArrayList<List<String>>() {{
                        add(new ArrayList<String>() {{
                            add("example");
                        }});
                    }}
            ));

        }};
    }
}
