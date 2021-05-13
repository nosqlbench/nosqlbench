package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.vladsch.flexmark.ast.FencedCodeBlock;

import java.util.Objects;

final class TestSet {
    private final String desc;
    public CharSequence info;
    public CharSequence text;

    public TestSet(String description, CharSequence info, CharSequence text) {
        this.desc = description;
        this.info = info;
        this.text = text;
    }

    public TestSet(String description, FencedCodeBlock node) {
        this.desc = description;
        this.info = node.getInfo();
        text = Objects.requireNonNull(node.getFirstChild()).getChars();
    }

    public String getDesc() {
        return desc;
    }
}
