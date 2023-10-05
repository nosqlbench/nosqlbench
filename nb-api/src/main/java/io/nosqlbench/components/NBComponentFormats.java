package io.nosqlbench.components;

import java.util.Iterator;

public class NBComponentFormats {
    public static String formatAsTree(NBBaseComponent base) {
        StringBuilder sb = new StringBuilder();
        PrintVisitor pv = new PrintVisitor(sb);
        NBComponentTraversal.visitDepthFirst(base,pv);
        return sb.toString();
    }

    private final static class PrintVisitor implements NBComponentTraversal.Visitor {

        private final StringBuilder builder;

        public PrintVisitor(StringBuilder sb) {
            this.builder = sb;
        }

        @Override
        public void visit(NBComponent component, int depth) {
            builder.append(String.format("%03d %s\n",depth,component));
        }
    }

}
