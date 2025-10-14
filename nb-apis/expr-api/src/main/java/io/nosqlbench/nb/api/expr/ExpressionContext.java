package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
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


import java.net.URI;
import java.util.Optional;

/**
 * Tracks the location context of a Groovy expression within a workload template.
 * Used to provide detailed error messages when expression evaluation fails.
 */
class ExpressionContext {
    private final Optional<URI> sourceUri;
    private final String expressionText;
    private final int templateLineNumber;
    private final int templateColumnStart;
    private final int templateColumnEnd;
    private final String templateLine;

    ExpressionContext(
        Optional<URI> sourceUri,
        String expressionText,
        int templateLineNumber,
        int templateColumnStart,
        int templateColumnEnd,
        String templateLine
    ) {
        this.sourceUri = sourceUri;
        this.expressionText = expressionText;
        this.templateLineNumber = templateLineNumber;
        this.templateColumnStart = templateColumnStart;
        this.templateColumnEnd = templateColumnEnd;
        this.templateLine = templateLine;
    }

    public Optional<URI> getSourceUri() {
        return sourceUri;
    }

    public String getExpressionText() {
        return expressionText;
    }

    public int getTemplateLineNumber() {
        return templateLineNumber;
    }

    public int getTemplateColumnStart() {
        return templateColumnStart;
    }

    public int getTemplateColumnEnd() {
        return templateColumnEnd;
    }

    public String getTemplateLine() {
        return templateLine;
    }

    public String getSourceDescription() {
        return sourceUri.map(URI::toString).orElse("<inline>");
    }
}
