package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.function.LongToIntFunction;

/**
 * Return a pseudo-randomly selected integer value from a file of numeric values.
 * Each line in the file must contain one parsable integer value.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashedLineToInt implements LongToIntFunction {
    private final static Logger logger  = LogManager.getLogger(HashedLineToInt.class);
    private final int[] values;
    private final String filename;
    private final Hash intHash;

    public HashedLineToInt(String filename) {
        this.filename = filename;
        List<String> lines = NBIO.readLines(filename);
        this.values = lines.stream().mapToInt(Integer::parseInt).toArray();
        this.intHash = new Hash();
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public int applyAsInt(long value) {
        int itemIdx = intHash.applyAsInt(value) % values.length;
        return values[itemIdx];
    }
}

