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

package io.nosqlbench.engine.api.scenarios;

import io.nosqlbench.nb.api.errors.BasicError;

final class SCNamedParam {
    private final String name;
    private final String operator;
    private final String value;
    private String scenarioName;

    public SCNamedParam(String name, String operator, String value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    public boolean isReassignable() {
        return NBCLIScenarioPreprocessor.UNLOCKED.equals(operator);
    }

    public boolean isFinalSilent() {
        return NBCLIScenarioPreprocessor.SILENT_LOCKED.equals(operator);
    }

    public boolean isFinalVerbose() {
        return NBCLIScenarioPreprocessor.VERBOSE_LOCKED.equals(operator);
    }


    public SCNamedParam override(String value) {
        if (isReassignable()) {
            return new SCNamedParam(this.name, this.operator, value);
        } else if (isFinalSilent()) {
            return this;
        } else if (isFinalVerbose()) {
            throw new BasicError("Unable to reassign value for locked param '" + name + operator + value + "'");
        } else {
            throw new RuntimeException("impossible!");
        }
    }

    @Override
    public String toString() {
        return name + (operator != null ? "=" : "") + (value != null ? value : "");
    }

    public String getName() {
        return name;
    }
}
