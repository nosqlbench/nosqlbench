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

public interface ArgType {
    enum TypeName {
        RefArg(RefArg.class,VariableRef.class),
        FunctionCall(FunctionCall.class,FunctionCall.class),
        StringArg(StringArg.class, String.class),
        FloatArg(FloatArg.class, float.class),
        DoubleArg(DoubleArg.class, double.class),
        LongArg(LongArg.class, long.class),
        IntegerArg(IntegerArg.class, int.class),
        BooleanArg(BooleanArg.class, boolean.class);

        private final Class<?> valueClass;
        private final Class<?> typeClass;

        TypeName(Class<?> typeClass, Class<?> valueClass) {
            this.typeClass = typeClass;
            this.valueClass= valueClass;
        }

        public Class<?> getTypeClass() {
            return this.typeClass;
        }
        public Class<?> getValueClass() {
            return this.valueClass;
        }

        public static TypeName valueOf(ArgType argType) {
            for (TypeName typeName : values()) {
                if (typeName.typeClass ==argType.getClass()) {
                    return typeName;
                }
            }
            throw new RuntimeException("Unable to resolve TypeName in " + ArgType.class.getCanonicalName() + " for " + argType.getClass().getCanonicalName());
        }
    }
}
