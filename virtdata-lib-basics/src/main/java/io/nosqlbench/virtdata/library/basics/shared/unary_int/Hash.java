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

package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.nio.ByteBuffer;
import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
@Categories({Category.general})
public class Hash implements IntUnaryOperator {

    private final transient ThreadLocal<Murmur3F> murmur3f_TL = ThreadLocal.withInitial(Murmur3F::new);

    @Override
    public int applyAsInt(int operand) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        Murmur3F murmur3f = murmur3f_TL.get();
        murmur3f.reset();
        bb.putInt(0,operand);
        bb.putInt(4,operand);
        bb.position(0);
        murmur3f.update(bb.array());
        long result= Math.abs(murmur3f.getValue());
        return (int) result & Integer.MAX_VALUE;
    }
}
