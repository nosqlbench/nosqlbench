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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Select a value from a text file line by modulo division against the number
 * of lines in the file.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ModuloLineToString implements LongFunction<String> {
    private final static Logger logger  = LogManager.getLogger(ModuloLineToString.class);
    private List<String> lines = new ArrayList<>();

    private final String filename;

    public ModuloLineToString(String filename) {
        this.filename = filename;
        this.lines = NBIO.readLines(filename);

    }

    @Override
    public String apply(long input) {
        int itemIdx = (int) (input % lines.size()) % Integer.MAX_VALUE;
        String item = lines.get(itemIdx);
        return item;
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }


}
