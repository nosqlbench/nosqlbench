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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bigint;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.math.BigInteger;
import java.util.function.LongFunction;

/**
 * Return a {@code BigInteger} value as the result of modulo division with the specified divisor.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ModuloToBigInt implements LongFunction<BigInteger> {
    private final static Logger logger  = LogManager.getLogger(ModuloToBigInt.class);
    private final long modulo;

    public ModuloToBigInt() {
        this.modulo = Long.MAX_VALUE;
    }

    public ModuloToBigInt(long modulo) {
        this.modulo=modulo;
    }

    @Override
    public BigInteger apply(long value) {
        long ret = (value % modulo) & Long.MAX_VALUE;
        return BigInteger.valueOf(ret);
    }
}
