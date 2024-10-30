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

package io.nosqlbench.engine.cmdstream;

import io.nosqlbench.engine.api.scenarios.NBCLIScenarioPreprocessor;
import io.nosqlbench.engine.core.lifecycle.session.CmdParser;
import io.nosqlbench.nb.api.errors.BasicError;

import java.util.Set;

/**
 * An argument to a command is based on a defined parameter. You can not assign a typed argument value to a
 * command without first resolving the specific parameter.
 */
public class CmdArg {
    CmdParam param;
    private final String operator;
    private final String value;

    private final static Set<String> OPERATORS = Set.of("=","==","===");
    public CmdArg(CmdParam param, String operator, String value) {
        if (!OPERATORS.contains(operator)) {
            throw new BasicError("You can't use the assignment operator '" + operator + "' with arguments to commands.");
        }
        this.param = param;
        this.operator = operator;
        this.value = value;
    }

    public static CmdArg of(String cmdName, String varname, String equals, String value) {
        CmdType type = CmdType.valueOfAnyCaseOrIndirect(cmdName);
        if (type==CmdType.indirect) {
            return new CmdArg(new CmdParam<String>(varname, s->s, false),equals,value);
        } else {
            return type.getNamedParam(varname).assign(equals,value);
        }
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


    public CmdArg override(String value) {
        if (isReassignable()) {
            return new CmdArg(this.param, this.operator, value);
        } else if (isFinalSilent()) {
            return this;
        } else if (isFinalVerbose()) {
            throw new BasicError("Unable to reassign value for locked param '" + param + operator + value + "'");
        } else {
            throw new RuntimeException("impossible!");
        }
    }

    public CmdParam getParam() {
        return param;
    }

    public String getValue() {
        return value;
    }

    public String getQuotedValue() {
        String quoted=value;
        for (char c :quoted.toCharArray()){
            if (CmdParser.SYMBOLS.indexOf(c)>=0) {
                quoted = "'" + quoted +"'";
                break;
            }
        }
        return quoted;
    }

    @Override
    public String toString() {
        return this.getParam().getName() + this.operator + getQuotedValue();
    }
}
