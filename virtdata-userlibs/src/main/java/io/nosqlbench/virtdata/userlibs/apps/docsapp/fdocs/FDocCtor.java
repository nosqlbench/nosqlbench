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

import io.nosqlbench.virtdata.api.processors.DocCtorData;

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
            sb.append("  - *example:* `").append(example.get(0)).append("`\n");
            if (example.size()>1) {
                sb.append("  - *").append(example.get(1)).append("*\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
