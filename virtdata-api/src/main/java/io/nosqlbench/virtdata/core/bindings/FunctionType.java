package io.nosqlbench.virtdata.core.bindings;

import java.util.function.*;

/**
 * <p>Captures the list of function object types which may be used
 * to implement data mapping functions. Library implementations
 * may rely on this for type metadata.</p>
 */
public enum FunctionType {

    long_long(LongUnaryOperator.class, long.class, long.class),
    long_int(LongToIntFunction.class, long.class, int.class),
    long_double(LongToDoubleFunction.class, long.class, double.class),
    long_T(LongFunction.class, long.class, Object.class),
    int_int(IntUnaryOperator.class, int.class, int.class),
    int_long(IntToLongFunction.class, int.class, long.class),
    int_double(IntToDoubleFunction.class, int.class, double.class),
    int_T(IntFunction.class, int.class, Object.class),
    double_T(DoubleFunction.class, double.class, Object.class),
    double_double(DoubleUnaryOperator.class, double.class, double.class),
    double_int(DoubleToIntFunction.class, double.class, int.class),
    double_long(DoubleToLongFunction.class, double.class, long.class),
    R_T(Function.class, Object.class, Object.class);

    private final Class<?> functionClass;
    private final Class<?> inputClass;
    private final Class<?> returnClass;
    private final ValueType returnValueType;
    private final ValueType inputValueType;

    FunctionType(Class<?> functionClass, Class<?> inputClass, Class<?> returnClass) {
        this.functionClass = functionClass;
        this.inputClass = inputClass;
        this.returnClass = returnClass;
        this.returnValueType = ValueType.valueOfAssignableClass(returnClass);
        this.inputValueType = ValueType.valueOfAssignableClass(inputClass);
    }
    public Class<?> getInputClass() {
        return inputClass;
    }
    public Class<?> getReturnClass() {
        return returnClass;
    }
    public Class<?> getFunctionClass() {
        return functionClass;
    }

    public static FunctionType valueOf(Class<?> clazz) {
        for(FunctionType functionType: FunctionType.values()) {
            if (functionType.functionClass==clazz) {
                return functionType;
            }
        }
        throw new RuntimeException("Unable to determine FunctionType for object class:" + clazz);
    }
    public static FunctionType valueOf(Object g) {
        for (FunctionType functionType : FunctionType.values()) {
            if (functionType.functionClass.isAssignableFrom(g.getClass())) {
                return functionType;
            }
        }
        throw new RuntimeException("Unable to determine FunctionType for object class:" + g.getClass());
    }

    public ValueType getInputValueType() {
        return inputValueType;
    }
}
