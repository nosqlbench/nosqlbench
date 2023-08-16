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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sequence<T> implements OpSequence<T> {
    private final SequencerType type;
    private final List<T> elems;
    private final int[] seq;

    Sequence(SequencerType type, List<T> elems, int[] seq) {
        this.type = type;
        this.elems = elems;
        this.seq = seq;
    }

    @Override
    public T apply(long selector) {
        int index = (int) (selector % seq.length);
        index = seq[index];
        return elems.get(index);
    }

    @Override
    public List<T> getOps() {
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
    public <U> Sequence<U> transform(Function<T, U> func) {
        return new Sequence<U>(type, elems.stream().map(func).collect(Collectors.toList()), seq);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.seq);
    }
}
