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

package io.nosqlbench.nbvectors.buildhdf5.predicates.types;

import java.nio.ByteBuffer;

public sealed interface PNode<T> extends BBWriter<T> permits ConjugateNode, PredicateNode {
    public static PNode<?> fromBuffer(ByteBuffer b) {
        byte typeOrdinal = b.get(b.position());
        return switch(ConjugateType.values()[typeOrdinal]) {
            case AND, OR -> new ConjugateNode(b);
            case PRED -> new PredicateNode(b);
        };
    }
}
