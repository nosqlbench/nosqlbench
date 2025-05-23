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

package io.nosqlbench.virtdata.library.basics.shared.from_string;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.text.StringEscapeUtils;

import java.util.function.Function;

/**
 * Escape all special characters which are required to be escaped when found within
 * JSON content according to the JSON spec
 * <pre>{@code
 * \b  Backspace (ascii code 08)
 * \f  Form feed (ascii code 0C)
 * \n  New line
 * \r  Carriage return
 * \t  Tab
 * \"  Double quote
 * \\  Backslash character
 * \/  Forward slash
 * }</pre>
 */
@ThreadSafeMapper
@Categories({Category.conversion, Category.general})
public class EscapeJSON implements Function<String,String> {
    Gson gson = new GsonBuilder().create();

    @Override
    public String apply(String s) {
        return StringEscapeUtils.escapeJson(s);
    }
}
