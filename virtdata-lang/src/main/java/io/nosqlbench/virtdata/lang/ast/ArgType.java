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
