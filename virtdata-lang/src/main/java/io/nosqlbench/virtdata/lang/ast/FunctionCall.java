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

package io.nosqlbench.virtdata.lang.ast;

import java.util.ArrayList;
import java.util.List;

public class FunctionCall implements ArgType {
    private String funcName;
    private final List<ArgType> args = new ArrayList<>();
    private String inputType;
    private String outputType;
    private String inputClass;

    public FunctionCall() {}

    public FunctionCall(String funcName) {
        this.funcName = funcName;
    }

    public FunctionCall(String inputType, String funcName) {
        this.inputType = inputType;
        this.funcName = funcName;
    }

    public void addFunctionArg(ArgType argType) {
        this.args.add(argType);
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(inputType==null ? "" : inputType + "->");
        sb.append(funcName);
        sb.append("(");
        String sep = "";
        for (ArgType arg : args) {
            sb.append(sep);
            sb.append(arg);
            sep=",";
        }
        sb.append(")");
        sb.append(outputType==null ? "" : "->"+ outputType);
        return sb.toString();
    }

    public FunctionCall getVirtdataCall(int i) {
        return (FunctionCall) args.get(i);
    }

    public String getFunctionName() {
        return funcName;
    }

    public String getInputType() {
        return inputType;
    }

    public String getOutputType() {
        return outputType;
    }

    public List<ArgType> getArgs() {
        return this.args;
    }

    public Object[] getArguments() {
        List<Object> args = new ArrayList<>();
        for (ArgType argType : getArgs()) {
            ArgType.TypeName typeName = ArgType.TypeName.valueOf(argType);
            switch (typeName) {
                case RefArg:
                    args.add(new VariableRef(((RefArg) argType).getRefName()));
                    break;
                case FunctionCall:
                    args.add(argType); // TODO: revisit this
                    break;
                case StringArg:
                    args.add(((StringArg) argType).getStringValue());
                    break;
                case FloatArg:
                    args.add(((FloatArg) argType).getFloatValue());
                    break;
                case IntegerArg:
                    args.add(((IntegerArg) argType).getIntValue());
                    break;
                case LongArg:
                    args.add(((LongArg) argType).getLongValue());
                    break;
                case DoubleArg:
                    args.add(((DoubleArg) argType).getDoubleValue());
                    break;
                case BooleanArg:
                    args.add(((BooleanArg) argType).getBooleanValue());
                    break;
                default:
                    throw new RuntimeException("Could not map type into argument object: " + typeName);
            }
        }
        return args.toArray();
    }

}
