package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

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


import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.processors.DocCtorData;
import io.nosqlbench.virtdata.api.processors.DocForFuncCtor;
import io.nosqlbench.virtdata.api.processors.DocFuncData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Example class doc part 2.
 *
 * Example unordered list part 2:
 * <UL>
 *     <LI>An item1 part 2</LI>
 *     <LI>An item2 part 2</LI>
 * </UL>
 *
 * An example class doc paragraph.
 *
 */
public class ExampleDocFunc2 implements DocFuncData {

    @Override
    public String getPackageName() {
        return "package.name.two";
    }

    @Override
    public Category[] getCategories() {
        return new Category[] { Category.general };
    }

    @Override
    public String getClassName() {
        return "ClassName2";
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
        examples.add(new ArrayList<>() {{ add("example"); add("one"); }});
        DocForFuncCtor ctordoc = new DocForFuncCtor("ClassName2", "ctordoc", args, examples);
        ctors.add(ctordoc);

        return ctors;
    }

    public List<DocForFuncCtor> getCtorsAlternate() {
        return new ArrayList<>() {{
            add(new DocForFuncCtor("ClassName2", "ctordoc",
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
            add(new DocForFuncCtor("ClassName2", "ctordoc",
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
