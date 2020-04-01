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

import io.nosqlbench.virtdata.api.annotations.Description;

import java.util.function.LongUnaryOperator;

/**
 * Integer devide the cycle, the other side of modulo.
 */
@Description("divide the long input by a long divisor, returning a long result")
public class DivideToLong implements LongUnaryOperator {

    private final long divisor;

    public DivideToLong(long divisor) {
        this.divisor=divisor;
    }

    @Override
    public long applyAsLong(long input) {
        return (input / divisor);
    }
}
