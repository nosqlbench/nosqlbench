package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.vladsch.flexmark.util.ast.Node;

import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Supplier;

final class TestSet {
    private final String description;
    private final Path path;
    private final int line;
    private final Node refnode;
    public CharSequence info;
    public CharSequence text;

    public TestSet(Supplier<CharSequence> desc, Node infoNode, Node dataNode, Path path) {
        this.description = desc.get().toString();
        this.info = infoNode.getChars();
        this.text = dataNode.getFirstChild().getChars();
        this.line = dataNode.getFirstChild().getLineNumber();
        this.path = path;
        this.refnode = dataNode;
    }

    public String getDesc() {
        return description;
    }

    public Path getPath() {
        return path;
    }

    public int getLine() {
        return line;
    }

    public Node getRefNode() {
        return refnode;
    }

    /**
     * Provide the logical path of the file being examined in this test set.
     * If the system properties indicate that the test is being run from within intellij,
     * the path will be relatized from the next module level up to allow for hot linking
     * directly to files.
     * @return A useful relative path to the file being tested
     */
    public String getLocationRef() {
        boolean inij = System.getProperty("sun.java.command","").toLowerCase(Locale.ROOT).contains("intellij");
        Path vcwd = Path.of(".").toAbsolutePath().normalize();
        vcwd = inij ? vcwd.getParent().normalize() : vcwd;
        Path relpath = vcwd.relativize(this.path.toAbsolutePath());
        if (inij) {
            relpath = Path.of(relpath.toString().replace("target/classes/","src/main/resources/"));
        }
        return "\t at (" + relpath + ":" + this.getLine() + ")";
    }

    @Override
    public String toString() {
        return this.getDesc();
    }
}
