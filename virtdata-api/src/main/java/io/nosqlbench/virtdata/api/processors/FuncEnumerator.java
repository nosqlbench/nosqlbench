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


import io.nosqlbench.virtdata.api.annotations.Category;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This enumerator receives stateful updates out of order from the annotation
 * processor and constructs consistent view of document models for listeners.
 */
public class FuncEnumerator {

    private final Types types;
    private final Elements elements;
    private final Filer filer;

    private DocForFunc model;
    private List<Listener> listeners = new ArrayList<>();
    private String anchorPackage;
    private String anchorSimpleName;

    public FuncEnumerator(Types types, Elements elements, Filer filer) {
        this.types = types;
        this.elements = elements;
        this.filer = filer;
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void onClass(String packageName, String simpleClassName, String classJavadoc) {
        if (model!=null) {
            throw new RuntimeException("The DocModel was overwritten. Perhaps you are not calling flush() ?");
        }
        model = new DocForFunc();
        model.setPackageName(packageName);
        model.setClassName(simpleClassName);
        model.setClassJavadoc(classJavadoc);
    }

    public void onApplyTypes(String inType, String outType) {
        model.setInType(inType);
        model.setOutType(outType);
    }

    public void onConstructor(LinkedHashMap<String, String> args, String ctorJavaDoc, List<List<String>> examples) {
        model.addCtor(ctorJavaDoc, args, examples);
    }

    public void flush() {
        this.listeners.forEach(l -> l.onFunctionModel(model));
        model=null;
    }

    public void onCategories(Category[] categories) {
        model.addCategories(categories);
    }

    /**
     * These Listeners handle data that has been found by the FuncEnumerator.
     */
    public interface Listener {
        /**
         * Handle each logical function model that has been found.
         * @param functionDoc the documentation model for a single mapping function
         */
        public void onFunctionModel(DocForFunc functionDoc);

    }
}
