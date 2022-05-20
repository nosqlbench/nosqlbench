package io.nosqlbench.virtdata.api.processors;

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


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.nosqlbench.virtdata.api.processors.ProcessorClassNames.PerThreadMapper;
import static io.nosqlbench.virtdata.api.processors.ProcessorClassNames.ThreadSafeMapper;

/**
 * This annotation processor is responsible for finding all annotated functions and adding
 * them to the manifest file for the current project.
 * Specifically, any class annotated as {@link io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper}
 * or {@link io.nosqlbench.virtdata.api.annotations.PerThreadMapper} are recorded in
 * <pre>classes/META-INF/functions</pre>
 *
 * This operates slightly differently than the service loader facility. The point is to
 * enumerate candidate functions without requiring them to have a no-args constructor.
 */
@SupportedOptions({"title"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes({
        ThreadSafeMapper,
        PerThreadMapper
})
public class FunctionManifestProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messenger;
    private Writer writer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messenger = processingEnv.getMessager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        try {
            if (writer==null) {
                writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/functions")
                        .openWriter();
            }
            for (String annotationType : this.getSupportedAnnotationTypes()) {
                Class<? extends Annotation> annotationClass =
                        (Class<? extends Annotation>) Class.forName(annotationType);
                Set<? extends Element> tsms = roundEnv.getElementsAnnotatedWith(annotationClass);
                if (tsms.size() > 0) {
                    for (Element e : tsms) {
                        writer.write(((TypeElement) e).getQualifiedName() + "\n");
                    }
                }
            }

            writer.close();

        } catch (Exception e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
        return false;
    }
}
