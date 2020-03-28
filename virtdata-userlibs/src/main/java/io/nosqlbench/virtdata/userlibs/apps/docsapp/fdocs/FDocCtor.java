package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import io.nosqlbench.virtdata.processors.DocCtorData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FDocCtor {

    private final List<List<String>> examples;
    private final String ctorJavaDoc;
    private final Map<String, String> args;
    private final String in;
    private final String out;
    private final String className;

    public FDocCtor(DocCtorData docCtorData, String in, String out) {
        this.examples =docCtorData.getExamples();
        this.ctorJavaDoc = docCtorData.getCtorJavaDoc();
        this.args = docCtorData.getArgs();
        this.className=docCtorData.getClassName();
        this.in = in;
        this.out = out;
    }

    public String asMarkdown() {
        StringBuilder sb = new StringBuilder();
        // - in->Name(arg1: type1, ...) ->out
        sb.append("- ").append(in).append(" -> ");
        sb.append(className).append("(");
        String args = this.args.entrySet().stream().map(e -> e.getValue() + ": " + e.getKey()).collect(Collectors.joining(", "));
        sb.append(args);
        sb.append(") -> ").append(out).append("\n");

        if (!ctorJavaDoc.isEmpty()) {
            sb.append("  - *notes:* ").append(ctorJavaDoc).append("\n");
        }
        for (List<String> example : examples) {
            sb.append("  - *ex:* `").append(example.get(0)).append("`\n");
            if (example.size()>1) {
                sb.append("  - *").append(example.get(1)).append("*\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
