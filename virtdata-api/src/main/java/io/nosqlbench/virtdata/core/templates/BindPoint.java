package io.nosqlbench.virtdata.core.templates;

import java.util.Objects;

public class BindPoint {
    private final String anchor;
    private final String bindspec;

    public BindPoint(String anchor, String bindspec) {
        this.anchor = anchor;
        this.bindspec = bindspec;
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

        if (!Objects.equals(anchor, bindPoint.anchor)) return false;
        return Objects.equals(bindspec, bindPoint.bindspec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anchor, bindspec);
    }

    @Override
    public String toString() {
        return "BindPoint{" +
                "anchor='" + anchor + '\'' +
                ", bindspec='" + bindspec + '\'' +
                '}';
    }
}
