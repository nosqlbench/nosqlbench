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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;

public class NBComponentFormats {
    public static String formatAsTree(NBBaseComponent base) {
        StringBuilder sb = new StringBuilder();
        PrintVisitor pv = new PrintVisitor(sb);
        NBComponentTraversal.visitDepthFirst(base,pv);
        return sb.toString();
    }

    protected final static class PrintVisitor implements NBComponentTraversal.Visitor {

        private final StringBuilder builder;

        public PrintVisitor(StringBuilder sb) {
            this.builder = sb;
        }

        @Override
        public void visit(NBComponent component, int depth) {
            String indent = "  ".repeat(depth);
            builder.append(indent).append(String.format("%03d %s",depth,component.description()));
            String componentNativeTypeToString = component.toString();
            String[] toStringLines = componentNativeTypeToString.split(System.lineSeparator());
            for (String s : toStringLines) {
                builder.append(System.lineSeparator()).append(indent).append(" >").append(s);
            }
            if (component.getComponentMetrics().size()>0) {
                builder.append(System.lineSeparator()).append("metrics:");
                for (NBMetric componentMetric : component.getComponentMetrics()) {
                    builder.append(indent).append(System.lineSeparator()).append("  ").append(componentMetric.toString());
                }
            }

            builder.append(System.lineSeparator());
        }
    }

}
