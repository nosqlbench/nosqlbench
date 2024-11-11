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

public class NoHyphens implements NBAdvisorCondition<String> {

    private final Level level;

    public NoHyphens(Level level) {
        this.level = level;
    }

    @Override
    public boolean test(String string) {
        return string.contains("-");
    }

    @Override
    public Function<String, String> okMsg() {
        return s -> "String '" + s + "' does not contain hyphens";
    }

    @Override
    public Function<String, String> errMsg() {
        return s -> "String '" + s + "' should not contain hyphens";
    }

    @Override
    public Level level() {
        return this.level;
    }

    @Override
    public String getName() {
        return "no-hyphens";
    }
}
