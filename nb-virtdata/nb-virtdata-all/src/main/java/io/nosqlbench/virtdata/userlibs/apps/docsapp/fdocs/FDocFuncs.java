/*
 * Copyright (c) 2022 nosqlbench
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
            functionsByPackage.keySet().forEach(k -> logger.warn(" package: " + k));
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

    @Override
    public String toString() {
        return "FDocFuncs{" +
                "functionsByPackage=" + functionsByPackage +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
