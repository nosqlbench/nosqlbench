package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

import io.nosqlbench.nb.api.markdown.FlexParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * holds all FunctionDoc instances for the same basic function name
 */
public class FDocFuncs implements Iterable<FDocFunc> {
    private final static Logger logger  = LogManager.getLogger(FDocFuncs.class);

    private final Map<String, FDocFunc> functionsByPackage = new HashMap<>();
    private final String functionName;

    public FDocFuncs(String functionName) {
        this.functionName=functionName;
    }
    public String getFunctionName() {
        return this.functionName;
    }

    public void addFunctionDoc(FDocFunc FDocFunc) {
        String name = FDocFunc.getPackageName() + "." + FDocFunc.getClassName();
        if (functionsByPackage.containsKey(name)) {
            throw new RuntimeException("Name '" + name + " is already present.");
        }
        functionsByPackage.put(name, FDocFunc);
    }

    @Override
    public Iterator<FDocFunc> iterator() {
        List<FDocFunc> fdocs = new ArrayList<>(functionsByPackage.values());
        Collections.sort(fdocs);
        return fdocs.iterator();
    }

    public String getCombinedClassDocs() {
        List<String> cdocs = functionsByPackage.values().stream()
            .sorted()
            .map(f -> f.getClassJavaDoc().trim())
            .filter(s -> s.length() > 0)
            .collect(Collectors.toList());

        if (cdocs.size()!=1) {
            logger.warn("There were " + cdocs.size() + " class docs found for types named " + getFunctionName());
        }

        return String.join("\n\n",cdocs);
    }

    public String asMarkdown() {
        StringBuilder sb = new StringBuilder();

        sb.append("## ").append(getFunctionName()).append("\n\n");

        String classDocMarkdown = FlexParser.converter.convert(getCombinedClassDocs());
        sb.append(classDocMarkdown).append("\n");

        for (FDocFunc fdf : functionsByPackage.values()) {
            for (FDocCtor ctor : fdf.getCtors()) {
                sb.append(ctor.asMarkdown());
            }
        }
        return sb.toString()
            .replaceAll("java.lang.","")
            .replaceAll("java.util.","")
            .replaceAll("java.net.","")
            .replaceAll("java.io.","");
    }
}
