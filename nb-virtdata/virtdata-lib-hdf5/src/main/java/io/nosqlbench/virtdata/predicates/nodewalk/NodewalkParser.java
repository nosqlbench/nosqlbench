/*
 * Copyright (c) 2025 nosqlbench
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
 *
 */

package io.nosqlbench.virtdata.predicates.nodewalk;

import io.nosqlbench.nbvectors.buildhdf5.predicates.types.*;
import io.nosqlbench.virtdata.predicates.nodewalk.repr.CqlNodeRenderer;
import io.nosqlbench.virtdata.predicates.nodewalk.repr.H5JsonNodeRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

public class NodewalkParser {
    private static final Logger logger = LogManager.getLogger(NodewalkParser.class);
    private final NodeRepresenter representer;
    private int nodewalkVersion;

    public final String CQL = "cql";
    public final String JSON = "json";

    public NodewalkParser(String[] schema) {
        this(schema, "cql");
    }

    public NodewalkParser(String[] schema, String repType) {
        switch(repType) {
            case CQL -> representer = new CqlNodeRenderer(schema);
            case JSON -> representer = new H5JsonNodeRenderer(schema);
            default -> throw new RuntimeException("Unknown representation type: " + repType);
        }
    }

    public String parse(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        ConjugateType eType = ConjugateType.values()[bytes[1]];
        PNode<?> predicateNode = switch(eType) {
            case PRED -> new PredicateNode(buffer);
            case AND,OR -> new ConjugateNode(buffer);
        };
        String rendered = representer.apply(predicateNode);
        logger.debug(() -> "rendered: " + rendered);
        return rendered;
    }

}
