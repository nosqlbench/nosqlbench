/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/// This version of an [[OpSequence]] allows a sequence to be derived from another sequence
/// based on a mapping function. This is done lazily by default, to allow for incremental
/// initialization.
public class DerivedSequence<BASE, DERIVED> implements OpSequence<DERIVED> {
    private final OpSequence<BASE> baseTypeSequence;
    private final SequencerType type;
    private final List<DERIVED> elems;
    private final int[] seq;
    private final Function<BASE, DERIVED> deriveF;

    public DerivedSequence(OpSequence<BASE> baseTypeSequence, Function<BASE,DERIVED> deriveF) {
        this.baseTypeSequence = baseTypeSequence;
        this.deriveF = deriveF;
        this.type = baseTypeSequence.getSequencerType();
        this.elems = new ArrayList<>(baseTypeSequence.getOps().size());
        this.seq = baseTypeSequence.getSequence();
    }

    @Override
    public DERIVED apply(long selector) {
        int index = (int) (selector % seq.length);
        index = seq[index];
        return elems.get(index);
    }

    public DERIVED derive(long selector) {
        int index = (int) (selector % seq.length);
        if (elems.get(index)==null) {
            elems.set(index,this.deriveF.apply(baseTypeSequence.getOps().get(index)));
        }
        return elems.get(index);
    }

    @Override
    public List<DERIVED> getOps() {
        return elems;
    }

    @Override
    public int[] getSequence() {
        return seq;
    }

    public SequencerType getSequencerType() {
        return type;
    }

    @Override
    public <OTHER> DerivedSequence<DERIVED,OTHER> transform(Function<DERIVED, OTHER> func) {
        return new DerivedSequence<DERIVED,OTHER>(this, func);
    }

    @Override
    public String toString() {
        return "seq len="+seq.length + ", LUT=" + Arrays.toString(this.seq);
    }
}
