package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.util.ast.Node;

import java.nio.file.Path;
import java.util.Objects;

final class TestSet {
    private final String desc;
    private final Path path;
    private final int line;
    public CharSequence info;
    public CharSequence text;

    public TestSet(String desc, Node infoNode, Node dataNode, Path path) {
        this.desc = desc;
        this.info = infoNode.getChars();
        this.text = dataNode.getFirstChild().getChars();
        this.line = dataNode.getFirstChild().getLineNumber();
        this.path = path;
    }

    public TestSet(String description, CharSequence info, CharSequence text, Path path, int line) {
        this.desc = description;
        this.info = info;
        this.text = text;
        this.path = path;
        this.line = line;
    }

    public TestSet(String description, FencedCodeBlock node, Path path) {
        this.desc = description;
        this.info = node.getInfo();
        text = Objects.requireNonNull(node.getFirstChild()).getChars();
        this.path = path;
        this.line = node.getLineNumber();
    }

    public String getDesc() {
        return desc;
    }

    public Path getPath() {
        return path;
    }

    public int getLine() {
        return line;
    }
}
