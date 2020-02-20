/*
 *   Copyright 2018 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Description;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.lfsrs.MetaShift;

/**
 * This function provides a low-overhead shuffling effect without loading
 * elements into memory. It uses a bundled dataset of pre-computed
 * Galois LFSR shift register configurations, along with a down-sampling
 * method to provide amortized virtual shuffling with minimal memory usage.
 *
 * Essentially, this guarantees that every value in the specified range will
 * be seen at least once before the cycle repeats. However, since the order
 * of traversal of these values is dependent on the LFSR configuration, some
 * orders will appear much more random than others depending on where you
 * are in the traversal cycle.
 *
 * This function *does* yield values that are deterministic.
 */
@ThreadSafeMapper
@Description("Provides virtual shuffling extremely large numbers.")
public class Shuffle extends MetaShift.Func {

    private final long max;
    private final long min;
    private final long size;

    @Example({"Shuffle(11,99)","Provide all values between 11 and 98 inclusive, in some order, then repeat"})
    public Shuffle(long min, long maxPlusOne) {
        this(min, maxPlusOne, Integer.MAX_VALUE);
    }

    @Example({"Shuffle(11,99,3)","Provide all values between 11 and 98 inclusive, in some different (and repeatable) order, then repeat"})
    public Shuffle(long min, long maxPlusOne, int bankSelector) {
        super(MetaShift.Masks.forPeriodAndBankModulo((maxPlusOne-min),bankSelector));
        this.min = min;
        this.max = maxPlusOne;
        this.size = (max-min);
    }

    @Override
    public long applyAsLong(long register) {
        register = (register % size) +1;
        register = super.applyAsLong(register);
        while (register>size) {
            register = super.applyAsLong(register);
        }
        register+=min;
        return register;
    }

}
