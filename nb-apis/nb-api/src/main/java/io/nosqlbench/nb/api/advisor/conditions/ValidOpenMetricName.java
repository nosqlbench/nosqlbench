package io.nosqlbench.nb.api.advisor.conditions;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.nb.api.advisor.NBAdvisorCondition;
import org.apache.logging.log4j.Level;

import java.util.function.Function;
import java.util.regex.Pattern;

public class ValidOpenMetricName implements NBAdvisorCondition<String> {

    private final static Pattern pattern =
        Pattern.compile("(?<name>[a-zA-Z_:][a-zA-Z0-9_" + ":]*)");

    private final Level level;

    public ValidOpenMetricName(Level level) {
        this.level = level;
    }

    private static String according_to = " according to the OpenMetrics specification";

    @Override
    public Function<String, String> okMsg() {
        return string -> "String '" + string + "' is a valid metric name" + according_to;
    }

    @Override
    public Function<String, String> errMsg() {
        return string -> "String '" + string + "' is not a valid metric name" + according_to;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public String getName() {
        return "valid metric name";
    }

    @Override
    public boolean test(String s) {
        return !pattern.matcher(s).matches();
    }
}
