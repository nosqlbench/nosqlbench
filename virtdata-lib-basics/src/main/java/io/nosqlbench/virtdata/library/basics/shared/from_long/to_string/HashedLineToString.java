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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_int.HashInterval;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly selected String value from a single line of
 * the specified file.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashedLineToString implements LongFunction<String> {
    private final static Logger logger = LogManager.getLogger(HashedLineToString.class);
    private final HashInterval indexRange;

    private List<String> lines = new ArrayList<>();

    private final String filename;

    public HashedLineToString(String filename) {
        this.filename = filename;
        this.lines = NBIO.readLines(filename);
        if (lines.size()<1) {
            throw new BasicError("Read " + lines.size() + " lines from " + filename + ", empty files are not supported");
        }
        this.indexRange = new HashInterval(0, lines.size());
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public String apply(long operand) {
        int itemIdx = indexRange.applyAsInt(operand);
        String item = lines.get(itemIdx);
        return item;
    }

}
