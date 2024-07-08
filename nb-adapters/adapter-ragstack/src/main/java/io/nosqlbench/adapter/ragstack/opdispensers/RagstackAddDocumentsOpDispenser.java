/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.ragstack.opdispensers;

import io.nosqlbench.adapter.ragstack.RagstackDriverAdapter;
import io.nosqlbench.adapter.ragstack.ops.RagstackAddDocumentsOp;
import io.nosqlbench.adapter.ragstack.ops.RagstackBaseOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.PyObject;
import org.python.core.PyString;

import java.util.List;
import java.util.function.LongFunction;

public class RagstackAddDocumentsOpDispenser extends RagstackOpDispenser {
    private static final Logger logger = LogManager.getLogger(RagstackAddDocumentsOpDispenser.class);
    private final LongFunction<RagstackAddDocumentsOp> opFunction;

    public RagstackAddDocumentsOpDispenser(RagstackDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<RagstackAddDocumentsOp> createOpFunction(ParsedOp op) {
        LongFunction<List> docFunc = op.getAsRequiredFunction("documents", List.class);
        return (l) -> new RagstackAddDocumentsOp(
            spaceFunction.apply(l).getVstore(),
            ListToPyArray((List<String>) docFunc.apply(l))
        );
    }

    private PyObject[] ListToPyArray(List<String> stringList) {
        //TODO: Implement this method
        PyObject[] pyObjects = new PyObject[stringList.size()];

        for (int i = 0; i < stringList.size(); i++) {
            pyObjects[i] = new PyString(stringList.get(i));
        }

        return pyObjects;
    }

    @Override
    public RagstackBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
