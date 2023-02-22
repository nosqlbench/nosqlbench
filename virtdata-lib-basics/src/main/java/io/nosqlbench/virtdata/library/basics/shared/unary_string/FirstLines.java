/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.general})
public class FirstLines implements Function<Object, String> {

    private static final Logger logger = LogManager.getLogger(FirstLines.class);

    private String fileContents;
    private int numberOfLines;

    public FirstLines(String filePath) {
        this(filePath, 1);
    }

    public FirstLines(String filePath, Integer numberOfLines) {

        if (numberOfLines < 1) {
            throw new BasicError("numberOfLines specified must be greater than zero.");
        }

        this.numberOfLines = numberOfLines;
        this.fileContents = readFile(filePath, numberOfLines == 1);
    }

    @Override
    public String apply(Object value) {
        return fileContents;
    }

    private String readFile(String targetFile, boolean isFirstLineOnly) {
        try {
            final List<String> lines = NBIO.readLines(targetFile);
            if (lines.isEmpty()) {
                throw new BasicError(String.format("Unable to locate content for %s", this));
            }
            if (isFirstLineOnly) {
                return lines.get(0);
            } else {
                StringBuilder content = new StringBuilder();
                if (numberOfLines > lines.size()) {
                    this.numberOfLines = lines.size();
                }

                List<String> requestedLines = lines.subList(0, numberOfLines - 1);
                requestedLines.forEach(content::append);
                return content.toString();
            }
        } catch (Exception ex) {
            throw new BasicError(String.format("Unable to locate file %s: ", targetFile), ex);
        }
    }
}
