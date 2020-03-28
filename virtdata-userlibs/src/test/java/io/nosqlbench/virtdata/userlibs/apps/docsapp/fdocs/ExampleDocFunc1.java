package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.processors.DocCtorData;
import io.nosqlbench.virtdata.processors.DocForFuncCtor;
import io.nosqlbench.virtdata.processors.DocFuncData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Example class doc part 1.
 *
 * Example unordered list part 1:
 * <UL>
 *     <LI>An item1 part 1</LI>
 *     <LI>An item2 part 1</LI>
 * </UL>
 *
 * An example class doc paragraph.
 *
 */
public class ExampleDocFunc1 implements DocFuncData {

    @Override
    public String getPackageName() {
        return "package.name.one";
    }

    @Override
    public Category[] getCategories() {
        return new Category[] { Category.general };
    }

    @Override
    public String getClassName() {
        return "ClassName1";
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
        args.put("arg2", "val2");
        List<List<String>> examples = new ArrayList<>();
        examples.add(new ArrayList<>() {{ add("example"); add("one"); }});
        DocForFuncCtor ctordoc = new DocForFuncCtor("ClassName1", "ctordoc", args, examples);
        ctors.add(ctordoc);

        return ctors;
    }

    public List<DocForFuncCtor> getCtorsAlternate() {
        return new ArrayList<>() {{
            add(new DocForFuncCtor("ClassName1", "ctordoc",
                    new LinkedHashMap<>() {{
                        put("aname", "atype");
                    }},
                    new ArrayList<>() {{
                        add(new ArrayList<>() {{
                            add("example");
                            add("description");
                        }});
                    }}
            ));
            add(new DocForFuncCtor("ClassName1", "ctordoc",
                    new LinkedHashMap<>() {{
                        put("aname", "atype");
                    }},
                    new ArrayList<>() {{
                        add(new ArrayList<>() {{
                            add("example");
                        }});
                    }}
            ));

        }};
    }
}
