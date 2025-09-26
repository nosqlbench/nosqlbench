/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.api.processors;
//io.nosqlbench.virtdata.api.processors.FunctionDocInfoProcessor

//io.nosqlbench.virtdata.api.processors.FunctionDocInfoProcessor


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.nosqlbench.virtdata.api.processors.ProcessorClassNames.PerThreadMapper;
import static io.nosqlbench.virtdata.api.processors.ProcessorClassNames.ThreadSafeMapper;
/**
 * This documentation processor is responsible for finding all the enumerated that feed documentation
 * manifests. It simply calls listener interfaces to do the rest of the work.
 */
@SupportedOptions({"title"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes({
        ThreadSafeMapper,
        PerThreadMapper
})
public class FunctionDocInfoProcessor extends AbstractProcessor {

    public final static String AUTOSUFFIX = "AutoDocsInfo";

    private static final Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
    private Filer filer;
    private Map<String, String> options;
    private Elements elementUtils;
    private Messager messenger;
    private SourceVersion sourceVersion;
    private Types typeUtils;
    private FuncEnumerator enumerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.options = processingEnv.getOptions();
        this.elementUtils = processingEnv.getElementUtils();
        this.messenger = processingEnv.getMessager();
        this.sourceVersion = processingEnv.getSourceVersion();
        this.typeUtils = processingEnv.getTypeUtils();

        this.enumerator = new FuncEnumerator(this.typeUtils, this.elementUtils, this.filer);
//        enumerator.addListener(new StdoutListener());
//        enumerator.addListener(new YamlDocsEnumerator(this.filer, this.messenger));
        enumerator.addListener(new FunctionDocInfoWriter(this.filer, this.messenger, AUTOSUFFIX));

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Element> ts = new ArrayList<>();

        ts.addAll(roundEnv.getElementsAnnotatedWith(io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper.class));
        ts.addAll(roundEnv.getElementsAnnotatedWith(io.nosqlbench.virtdata.api.annotations.PerThreadMapper.class));

        for (Element classElem : ts) {

            if (classElem.getKind() != ElementKind.CLASS) {
                throw new RuntimeException("Unexpected kind of element: " + classElem.getKind() + " for " + classElem);
            }

            // package and Class Name

            Name qualifiedName = ((TypeElement) classElem).getQualifiedName();
            Matcher pnm = packageNamePattern.matcher(qualifiedName);
            if (!pnm.matches()) {
                throw new RuntimeException("Unable to match qualified name for package and name: " + qualifiedName);
            }
            String packageName = pnm.group("packageName");
            String simpleClassName = pnm.group("className");

            // Class JavaDoc

            String classDoc = elementUtils.getDocComment(classElem);
            classDoc = classDoc == null ? "" : cleanJavadoc(classDoc);
            classDoc = inheritDocs(classDoc,classElem);

            enumerator.onClass(packageName, simpleClassName, classDoc);

            Categories categoryAnnotation = classElem.getAnnotation(Categories.class);
            if (categoryAnnotation!=null) {
                Category[] value = categoryAnnotation.value();
                enumerator.onCategories(value);
            } else {
                messenger.printMessage(Diagnostic.Kind.ERROR,"@Categories is a required annotation", classElem);
            }
            // apply method types

            // Build class hierarchy starting with current class
            List<TypeElement> classHierarchy = getClassHierarchy((TypeElement) classElem);

            // Search for apply method in class hierarchy
            Element applyMethodElem = null;
            for (TypeElement typeInHierarchy : classHierarchy) {
                applyMethodElem = findApplyMethodInType(typeInHierarchy);
                if (applyMethodElem != null) {
                    break;
                }
            }

            if (applyMethodElem == null) {
                messenger.printMessage(Diagnostic.Kind.ERROR, "Unable to enumerate input and output types for " + simpleClassName);
                return false;
            }

            VariableElement inParam = ((ExecutableElement) applyMethodElem).getParameters().get(0);
            String inType = inParam.asType().toString();
            String outType = ((ExecutableElement) applyMethodElem).getReturnType().toString();
            enumerator.onApplyTypes(inType, outType);

            // Ctors

            for (Element ctorElem : classElem.getEnclosedElements()) {
                if (ctorElem.getKind() == ElementKind.CONSTRUCTOR) {

                    // Ctor Args
                    List<? extends VariableElement> parameters = ((ExecutableElement) ctorElem).getParameters();
                    LinkedHashMap<String, String> args = new LinkedHashMap<>();
                    boolean isVarArgs = ((ExecutableElement) ctorElem).isVarArgs();
                    for (int i = 0; i < parameters.size(); i++) {
                        VariableElement var = parameters.get(i);
                        String varName = var.getSimpleName().toString();
                        String varType = var.asType().toString() + (i == parameters.size() - 1 ? (isVarArgs ? "..." : "") : "");
                        args.put(varName, varType);
                    }

                    // Ctor Javadoc
                    String ctorDoc = elementUtils.getDocComment(ctorElem);
                    ctorDoc = ctorDoc == null ? "" : cleanJavadoc(ctorDoc);

                    // Examples
                    List<List<String>> exampleData = new ArrayList<>();
                    Example[] exampleAnnotations = ctorElem.getAnnotationsByType(Example.class);
                    for (Example example : exampleAnnotations) {
                        example.value();
                        exampleData.add(Arrays.asList(example.value()));
                    }

                    enumerator.onConstructor(args, ctorDoc, exampleData);
                }

            }

            enumerator.flush();
        }

        return false;
    }

    private static final Pattern inheritDocPattern = Pattern.compile("(?ms)(?<pre>.*)(?<inherit>\\{@inheritDoc})(?<post>.*)$");
    private String inheritDocs(String classDoc, Element classElem) {
        if (classDoc==null) {
            return null;
        }
        Matcher matcher = inheritDocPattern.matcher(classDoc);
        if (!matcher.matches()) {
            return classDoc;
        }
        StringBuilder docData = new StringBuilder();
        String pre = matcher.group("pre");
        String post = matcher.group("post");

        Optional<TypeElement> inheritFromElement = Optional.ofNullable(((TypeElement) classElem).getSuperclass())
                .map(String::valueOf)
                .map(elementUtils::getTypeElement);


        if (!inheritFromElement.isPresent()) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "Element " + classElem + " has '{@inheritDoc}', but a superclass was not found.");
            return pre + "UNABLE TO FIND ELEMENT TO INHERIT DOCS FROM for " + classElem + " " + post;
        }
        TypeElement inheritFromType = inheritFromElement.get();
        String inheritedDocs = elementUtils.getDocComment(inheritFromType);
        if (inheritedDocs==null) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "javadocs are missing on " + inheritFromElement + ", but "
            + classElem + " is trying to inherit docs from it.");
            return pre + "UNABLE TO FIND INHERITED DOCS for " + classElem + " " + post;
        }

        if (inheritDocPattern.matcher(inheritedDocs).matches()) {
            return pre + inheritDocs(inheritedDocs,inheritFromType) + post;
        } else {
            return pre + inheritedDocs + post;
        }

    }

    private String cleanJavadoc(String ctorDoc) {
        return ctorDoc.replaceAll("(?m)^ ", "");
    }

    /**
     * Build an ordered list of types in the class hierarchy, starting with the given type
     * and walking up to its parent classes (excluding Object).
     */
    private List<TypeElement> getClassHierarchy(TypeElement startType) {
        List<TypeElement> hierarchy = new ArrayList<>();
        TypeElement current = startType;

        while (current != null) {
            hierarchy.add(current);

            // Get the superclass
            if (current.getSuperclass() != null &&
                !current.getSuperclass().toString().equals("java.lang.Object") &&
                !current.getSuperclass().toString().equals("none")) {
                current = elementUtils.getTypeElement(current.getSuperclass().toString());
            } else {
                current = null;
            }
        }

        return hierarchy;
    }

    /**
     * Find an apply method in the given type element.
     * Returns the first method whose name starts with "apply", or null if none found.
     */
    private Element findApplyMethodInType(TypeElement type) {
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                if (element.getSimpleName().toString().startsWith("apply")) {
                    return element;
                }
            }
        }
        return null;
    }

    private static class StdoutListener implements FuncEnumerator.Listener {
        @Override
        public void onFunctionModel(DocForFunc functionDoc) {
            System.out.println(functionDoc);
        }
    }
}
