/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bigdecimal;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.math.BigDecimal;
import java.util.function.LongFunction;

/**
 * Return a {@code BigDecimal} value as the result of modulo division with the specified divisor.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ModuloToBigDecimal implements LongFunction<BigDecimal> {
    private final static Logger logger  = LogManager.getLogger(ModuloToBigDecimal.class);
    private final long modulo;

    public ModuloToBigDecimal() {
        this.modulo = Long.MAX_VALUE;
    }

    public ModuloToBigDecimal(long modulo) {
        this.modulo=modulo;
    }

    @Override
    public BigDecimal apply(long value) {
        long ret = (value % modulo) & Long.MAX_VALUE;
        return BigDecimal.valueOf(ret);
    }
}
