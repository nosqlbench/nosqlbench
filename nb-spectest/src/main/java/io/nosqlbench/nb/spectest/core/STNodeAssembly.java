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

package io.nosqlbench.nb.spectest.core;

import com.vladsch.flexmark.util.ast.Node;
import io.nosqlbench.nb.spectest.testtypes.STNamedCodeTuples;

import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <P>A {@link STNodeAssembly} is a sequence of {@link STNode}s. This is meant to
 * group sets of test data together into a single cohesive specification which
 * contains its own set of documentation, examples, and full-content validation.
 * As such, the markdown which provides the specification should be usable directly
 * as detailed user-facing documentation.</P>
 *
 * <P>The method of discovery, assembly, and validation of a {@link STNodeAssembly} is
 * determined by the testing harness assembled around it. In any case, the type of
 * presentation structure and validation logic that accompanies a given assembly
 * should be self-evident, and documented clearly within the surrounding
 * markdown.</P>
 */
public class STNodeAssembly extends ArrayList<STNode> {

    public STNodeAssembly(STNode... testsets) {
        this.addAll(Arrays.asList(testsets));
    }

    @Override
    public String toString() {
        return (size()>0 ? get(0).toString() : "NONE");
    }

    public STNamedCodeTuples getAsNameAndCodeTuples() {
        if ((size()%2)==0) {
            List<STNameAndCodeTuple> tuples = new ArrayList<>();
            for (int i = 0; i < size(); i+=2) {
                tuples.add(new STNameAndCodeTuple(get(i),get(i+1)));
            }
            return new STNamedCodeTuples(tuples);
        } else {
            throw new InvalidParameterException("Impossible with an odd number of elements.");
        }
    }

    public String getDescription(int index) {
        assertRange(index);
        return get(index).getDesc();
    }

    public String getDescription() {
        if (size()==0) {
            return "";
        }
        return get(0).getDesc();
    }

    public String getAsText(int index) {
        assertRange(index);
        return get(index).text.toString();
    }

    public Path getPath() {
        if (size()==0) {
            return null;
        }
        return get(0).getPath();
    }

    public Node getRefNode(int index) {
        assertRange(index);
        return get(index).getRefNode();
    }

    private void assertRange(int index) {
        if (size()-1<index) {
            throw new InvalidParameterException("index " + index + " was out of bounds. Your pattern matching and access patterns to this data are not in sync.");
        }
    }

}
