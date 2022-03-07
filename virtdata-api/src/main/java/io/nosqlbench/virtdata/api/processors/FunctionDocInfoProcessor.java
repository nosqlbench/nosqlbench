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
import static io.nosqlbench.virtdata.api.processors.ProcessorClassNames.ThreadSafeMapper;/**
 * This documentation processor is responsible for finding all the enumerated that feed documentation
 * manifests. It simply calls listener interfaces to do the rest of the work.
 */
@SupportedOptions({"title"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        ThreadSafeMapper,
        PerThreadMapper
})
public class FunctionDocInfoProcessor extends AbstractProcessor {

    public final static String AUTOSUFFIX = "AutoDocsInfo";

    private static Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
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
                throw new RuntimeException("Unexpected kind of element: " + classElem.getKind() + " for " + classElem.toString());
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
            }
            // apply method types

            boolean foundApply=false;
            Element applyMethodElem = null;
            Element applyInClassElem = classElem;
            while (applyMethodElem==null && applyInClassElem!=null) {
                for (Element candidateApplyElem : applyInClassElem.getEnclosedElements()) {
                    if (candidateApplyElem.getKind() == ElementKind.METHOD) {
                        if (candidateApplyElem.getSimpleName().toString().startsWith("apply")) {
                            applyMethodElem = candidateApplyElem;
                            break;
                        }

                    }
                }
                if (applyMethodElem!=null) {
                    break;
                }
                applyInClassElem = elementUtils.getTypeElement(((TypeElement) applyInClassElem).getSuperclass().toString());
            }
            if (applyMethodElem==null) {
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

    private static Pattern inheritDocPattern = Pattern.compile("(?ms)(?<pre>.*)(?<inherit>\\{@inheritDoc})(?<post>.*)$");
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
            messenger.printMessage(Diagnostic.Kind.ERROR, "Element " + classElem.toString() + " has '{@inheritDoc}', but a superclass was not found.");
            return pre + "UNABLE TO FIND ELEMENT TO INHERIT DOCS FROM for " + classElem.toString() + " " + post;
        }
        TypeElement inheritFromType = inheritFromElement.get();
        String inheritedDocs = elementUtils.getDocComment(inheritFromType);
        if (inheritedDocs==null) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "javadocs are missing on " + inheritFromElement.toString() + ", but "
            + classElem.toString() + " is trying to inherit docs from it.");
            return pre + "UNABLE TO FIND INHERITED DOCS for " + classElem.toString() + " " + post;
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

    private static class StdoutListener implements FuncEnumerator.Listener {
        @Override
        public void onFunctionModel(DocForFunc functionDoc) {
            System.out.println(functionDoc);
        }
    }
}
