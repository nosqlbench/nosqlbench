package io.nosqlbench.virtdata.api.processors;

import com.squareup.javapoet.*;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.api.annotations.Category;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FunctionDocInfoWriter implements FuncEnumerator.Listener {

    private final String suffix;
    private Filer filer;
    private Messager messenger;

    public FunctionDocInfoWriter(Filer filer, Messager messenger, String suffix) {
        this.filer = filer;
        this.messenger = messenger;
        this.suffix = suffix;
    }

    @Override
    public void onFunctionModel(DocForFunc functionDoc) {

        TypeSpec typeSpec = this.createInlineClassForDocFuncData(functionDoc,
                functionDoc.getClassName() + suffix);

        JavaFile javafile = JavaFile.builder(functionDoc.getPackageName(), typeSpec)
                .addFileComment("This file is auto-generated.")
                .build();

        try {
            javafile.writeTo(this.filer);
        } catch (IOException e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "Error writing javafile " + javafile.packageName + "." + javafile.typeSpec.toString());
            throw new RuntimeException(e);
        }

    }

    private TypeSpec createInlineClassForDocFuncData(DocForFunc doc, String newClassName) {
        List<MethodSpec> methods = new ArrayList<>();

        MethodSpec getClassName = MethodSpec.methodBuilder("getClassName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", doc.getClassName())
                .build();
        methods.add(getClassName);

        MethodSpec getPackageName = MethodSpec.methodBuilder("getPackageName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", doc.getPackageName())
                .build();

        methods.add(getPackageName);

        MethodSpec getClassJavaDoc = MethodSpec.methodBuilder("getClassJavadoc")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", doc.getClassJavadoc())
                .build();
        methods.add(getClassJavaDoc);

        MethodSpec getInType = MethodSpec.methodBuilder("getInType")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", doc.getInType())
                .build();
        methods.add(getInType);

        MethodSpec getOutType = MethodSpec.methodBuilder("getOutType")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $S", doc.getOutType())
                .build();
        methods.add(getOutType);


        CodeBlock ctorsHead = CodeBlock.builder().add("return new $T<$T>() {{$>\n", ArrayList.class, DocCtorData.class).build();
        CodeBlock ctorsTail = CodeBlock.builder().add("$<}}").build();

        CodeBlock.Builder ctors = CodeBlock.builder().add(ctorsHead);
        for (DocCtorData ctor : doc.getCtors()) {
            ctors.add("add(new $T($S, $S, \n$>new $T<String, String>() {{\n$>", DocForFuncCtor.class, ctor.getClassName(), ctor.getCtorJavaDoc(), LinkedHashMap.class);
            for (Map.Entry<String, String> arg : ctor.getArgs().entrySet()) {
                ctors.add("put($S,$S);\n", arg.getKey(), arg.getValue());
            }
            ctors.add("$<}},\n");
            ctors.add("new $T<$T<$T>>() {{\n$>", ArrayList.class, List.class, String.class);
            for (List<String> example : ctor.getExamples()) {
                ctors.add("add(new $T<$T>() {{$>\n", ArrayList.class, String.class);
                for (String s : example) {
                    Matcher m = Pattern.compile(Matcher.quoteReplacement("$")).matcher(s);
                    s=m.replaceAll(m.quoteReplacement("$$"));
                    ctors.add("add(\"" + s + "\");\n");
                }
                ctors.add("$<}});\n");
            }
            ctors.add("$<}}");
            ctors.add("\n$<));\n");
        }
        ctors.add(ctorsTail);

        MethodSpec getCategoriesMethod = MethodSpec.methodBuilder("getCategories")
                .addModifiers(Modifier.PUBLIC)
                .returns(ArrayTypeName.of(Category.class))
                .addStatement("return new $T[] { " +
                        Arrays.stream(doc.getCategories()).map(c -> "Category." + c)
                                .collect(Collectors.joining(","))+" }", Category.class).build();
        methods.add(getCategoriesMethod);

        MethodSpec getCtorsMethod = MethodSpec.methodBuilder("getCtors")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(List.class, DocCtorData.class))
                .addStatement(ctors.build())
                .build();
        methods.add(getCtorsMethod);


        AnnotationSpec serviceAnnotation = AnnotationSpec.builder(Service.class)
                .addMember("value","$T.class",DocFuncData.class)
                .build();

        TypeSpec manifestType = TypeSpec.classBuilder(newClassName)
                .addAnnotation(serviceAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .addSuperinterface(DocFuncData.class)
                .build();

        return manifestType;
    }
}

