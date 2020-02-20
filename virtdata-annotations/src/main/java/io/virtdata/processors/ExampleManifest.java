package io.virtdata.processors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExampleManifest {

    public List<DocFuncData> getDocs() {
        ArrayList<DocFuncData> docFuncData = new ArrayList<>();
        docFuncData.add(new DocForFunc() {{
            setClassName("classname");
            setPackageName("packagename");
            setClassJavadoc("javadoc");
            setInType("intype");
            setOutType("outtype");
            addCtor("ctordoc",
                    new LinkedHashMap<String, String>() {{
                        put("vname", "vtype");
                    }},
                    new ArrayList<List<String>>() {{
                        add(new ArrayList<String>() {{
                            add("syntax");
                            add("comment)");
                        }});
                    }});
        }});

        return docFuncData;
    }
}
