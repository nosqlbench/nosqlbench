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

package io.nosqlbench.nb.spectest.loaders;

import com.vladsch.flexmark.ast.Heading;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Supplier;

public class STHeadingScanner implements Supplier<CharSequence> {
    private final LinkedList<Heading> tnodes = new LinkedList<>();
    private Path path;
    private int index;
    private final String delimiter;

    public STHeadingScanner(String delimiter) {
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
     * If this method is called, then the heading index is incremented to track
     * enumerations (represented as numeric indices) of headings within a common flow of elements
     * @return
     */
    public STHeadingScanner index() {
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
    public STHeadingScanner update(Object object) {
        if (object==null ) {
            updatePath(null);
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
