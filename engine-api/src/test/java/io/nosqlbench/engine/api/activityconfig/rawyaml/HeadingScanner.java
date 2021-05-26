package io.nosqlbench.engine.api.activityconfig.rawyaml;

import com.vladsch.flexmark.ast.Heading;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Supplier;

public class HeadingScanner implements Supplier<CharSequence> {
    private final LinkedList<Heading> tnodes = new LinkedList<>();
    private Path path;
    private int index;
    private final String delimiter;

    public HeadingScanner(String delimiter) {
        this.delimiter = delimiter;
    }

    public String toString(String delim) {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (Heading tnode : tnodes) {
            sb.append(prefix);
            sb.append(tnode.getText());
            if (tnode.getAnchorRefText()!=null && (!tnode.getAnchorRefText().equals(tnode.getText().toString()))) {
                sb.append("(").append("#".repeat(tnode.getLevel())).append(" ").append(tnode.getAnchorRefText()).append(")");
            }
            prefix=delim;
        }

        if (index>0) {
            sb.append(String.format(" (%02d)",index));
        }
        return sb.toString();
    }

    public String toString() {
        return toString(this.delimiter);
    }

    /**
     * If this method is called, then the heading is presented as
     * @return
     */
    public HeadingScanner index() {
        index++;
        return this;
    }

    /**
     * Update the state of the heading scanner.
     * If the object is a path, then the heading list is cleared and the new path is set.
     * If the object is a Heading node, then the heading list is updated according to the level
     * of the provided heading. All heading levels deeper than the provided one are removed from the list first.
     * Any other object type is ignored silently.
     *
     * @param object Any object which might be a Heading or Path
     * @return this HeadingScanner for method chaining
     */
    public HeadingScanner update(Object object) {
        if (object==null ) {
        } else if (object instanceof Path) {
            updatePath((Path)object);
        } else if (object instanceof Heading) {
            updateHeading((Heading) object);
        }
        return this;
    }

    private void updatePath(Path path) {
        if (this.path == null || this.path != path) {
            reset();
        }
        this.path = path;
    }

    private void reset() {
        this.tnodes.clear();
        this.path=null;
        this.index=0;
    }

    private void updateHeading(Heading heading) {

        ListIterator<Heading> nodes = tnodes.listIterator();
        while (nodes.hasNext()) {
            Heading node = nodes.next();
            if (node.getLevel()>=heading.getLevel()) {
                nodes.remove();
            }
        }
        nodes.add(heading);
        index=0;
    }

    @Override
    public CharSequence get() {
        return toString();
    }
}
