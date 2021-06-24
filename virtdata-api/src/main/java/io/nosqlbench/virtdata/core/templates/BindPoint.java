package io.nosqlbench.virtdata.core.templates;

import java.util.Objects;

public class BindPoint {
    private final String anchor;
    private final String bindspec;
    private final Type type;


    public enum Type {
        /**
         * a reference bindpoint is expressed as a single word within single curly braces like <pre>{@code {bindref}}</pre>
         */
        reference,
        /**
         * a definition bindpoint is expressed as anything between double curly braces like <pre>{@code {{Identity()}}</pre>
         */
        definition
    }

    public BindPoint(String anchor, String bindspec, Type type) {
        this.anchor = anchor;
        this.bindspec = bindspec;
        this.type = type;
    }

    public BindPoint(String anchor, String bindspec) {
        this.anchor = anchor;
        this.bindspec = bindspec;
        this.type=Type.reference;
    }

    public static BindPoint of(String userid, String bindspec, Type type) {
        return new BindPoint(userid,bindspec,type);
    }

    public Type getType() {
        return type;
    }

    public String getAnchor() {
        return anchor;
    }

    public String getBindspec() {
        return bindspec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BindPoint bindPoint = (BindPoint) o;
        return Objects.equals(anchor, bindPoint.anchor) && Objects.equals(bindspec, bindPoint.bindspec) && type == bindPoint.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anchor, bindspec, type);
    }

    @Override
    public String toString() {
        return "BindPoint{" +
            "anchor='" + anchor + '\'' +
            ", bindspec='" + bindspec + '\'' +
            ", type=" + type +
            '}';
    }
}
