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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Return the number of a random line from the specified file, indexed starting from 0.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ModuloLineOfFile implements LongUnaryOperator {
    private final static Logger logger  = LogManager.getLogger(ModuloLineOfFile.class);
    private final long lines;

    @Example({"ModuloLineOfFile('file_with_three_lines.txt')","return values from 0 to 2 inclusive"})
    public ModuloLineOfFile(String filename) {
        this.lines = NBIO.readLines(filename).size();
        if (lines==0) {
            throw new RuntimeException("found zero lines in '" + filename +"'");
        }
    }

    @Override
    public long applyAsLong(long input) {
        return (input % lines);
    }


}
