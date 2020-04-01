/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.function.LongUnaryOperator;

/**
 * Return a long value as the result of modulo division with the specified divisor.
 */
@ThreadSafeMapper
public class ModuloToLong implements LongUnaryOperator {
    private final static Logger logger  = LogManager.getLogger(ModuloToLong.class);
    private final long modulo;

    public ModuloToLong(long modulo) {
        this.modulo=modulo;
    }

    @Override
    public long applyAsLong(long input) {
        long ret = (input % modulo) & Long.MAX_VALUE;
        return ret;
    }

}
