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

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.nb.api.pathutil.NBPaths;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

import static io.nosqlbench.nb.api.pathutil.NBPaths.readDataFileLines;

/**
 * Select a value from a text file line by modulo division against the number
 * of lines in the file.
 */
@ThreadSafeMapper
public class ModuloLineToString implements LongFunction<String> {
    private final static Logger logger  = LogManager.getLogger(ModuloLineToString.class);
    private List<String> lines = new ArrayList<>();

    private String filename;

    public ModuloLineToString(String filename) {
        this.filename = filename;
        this.lines = readDataFileLines(filename);
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
