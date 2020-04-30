package io.nosqlbench.nb.annotations;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This annotation processor is responsible for adding services to the
 * <pre>classes/META-INF/services/servicename</pre> file for each
 * implemented and annotated service name.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_12)
public class ServiceProcessor extends AbstractProcessor {
    public final static String SERVICE_NAME = Service.class.getCanonicalName();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(SERVICE_NAME);
        return supportedAnnotations;
    }

    private static Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
    private Filer filer;
    private Map<String, String> options;
    private Elements elementUtils;
    private Messager messenger;
    private SourceVersion sourceVersion;
    private Types typeUtils;
    private Map<String, Writer> writers = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.options = processingEnv.getOptions();
        this.elementUtils = processingEnv.getElementUtils();
        this.messenger = processingEnv.getMessager();
        this.sourceVersion = processingEnv.getSourceVersion();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    private Writer getWriterForClass(String className, Element... elements) {
        return writers.computeIfAbsent(className, s -> {
            try {
                return filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + s, elements)
                        .openWriter();
            } catch (IOException e) {
                messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Element> ts = new ArrayList<>();

        try {
            for (String annotationType : this.getSupportedAnnotationTypes()) {
                Class<? extends Annotation> annotationClass =
                        (Class<? extends Annotation>) Class.forName(annotationType);
                Set<? extends Element> tsms = roundEnv.getElementsAnnotatedWith(annotationClass);

                for (Element element : tsms) {
                    String serviceClass = null;
                    for (AnnotationMirror am : element.getAnnotationMirrors()) {
                        DeclaredType atype = am.getAnnotationType();
                        if (!annotationType.equals(atype.toString())) {
                            continue;
                        }

                        List<? extends ExecutableElement> valueKeys = am.getElementValues().keySet().stream()
                                .filter(k -> k.toString().equals("value()")).collect(Collectors.toList());
                        if (valueKeys.size()==0) {
                            messenger.printMessage(Diagnostic.Kind.ERROR, "Annotation missing required value");
                            return false;
                        }
                        AnnotationValue annotationValue = am.getElementValues().get(valueKeys.get(0));
                        serviceClass = annotationValue.getValue().toString();
                    }

                    Writer w = getWriterForClass(serviceClass, tsms.toArray(new Element[0]));

                    Name name = ((TypeElement) element).getQualifiedName();
                    messenger.printMessage(Diagnostic.Kind.NOTE,"Adding service entry for implementation of " + serviceClass + ": " + name);
                    w.write(name + "\n");

                }
            }

            for (Writer writer : this.writers.values()) {
                writer.close();
            }

        } catch (Exception e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
        return true;
    }
}
