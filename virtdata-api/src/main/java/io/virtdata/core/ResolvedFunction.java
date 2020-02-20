package io.virtdata.core;

import io.virtdata.api.FunctionType;
import io.virtdata.api.ValueType;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * A function that has been resolved by a libraryName for use in data mapping.
 * Some API calls require this type, as it can only be constructed successfully
 * if the object type is valid for mapping to a data mapper function.
 */
public class ResolvedFunction {

    public static Comparator<ResolvedFunction> PREFERRED_TYPE_COMPARATOR = new PreferredTypeComparator();
    private final Class<?> inputType;
    private final Class<?> outputType;
    private Class<?>[] initializerSignature;
    private Object[] initializerValues;
    private FunctionType functionType;
    private Object functionObject;
    private String libraryName;
    private boolean isThreadSafe;
    private static MethodHandles.Lookup methodLookup = MethodHandles.publicLookup();


    public ResolvedFunction(Object g, boolean isThreadSafe, Class<?>[] initializerSignature, Object[] initValues, Class<?> inputType, Class<?> outputType, String libraryName) {
        this(g, isThreadSafe, initializerSignature, initValues, inputType, outputType);
        this.libraryName = libraryName;
    }

    public ResolvedFunction(Object g, boolean isThreadSafe, Class<?>[] initializerSignature, Object[] initValues, Class<?> inputType, Class<?> outputType) {
        this.functionObject = g;
        this.isThreadSafe = isThreadSafe;
        functionType = FunctionType.valueOf(g);  // sanity check the type of g
        this.initializerSignature = initializerSignature;
        this.initializerValues = initValues;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public static String getStringLegend() {
        return "[<library name>::] input->class->output [initializer type->parameter type,...]";
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public Object getFunctionObject() {
        return functionObject;
    }

    public void setFunctionObject(Object functionObject) {
        this.functionObject = functionObject;
    }

    public Class<?> getResultClass() {
        Method applyMethod = getMethod();
        return applyMethod.getReturnType();
    }

    public Class<?> getInputClass() {
        Method applyMethod = getMethod();
        return applyMethod.getParameterTypes()[0];
    }

    public Class<?> getArgType() {
        Method applyMethod = getMethod();
        if (applyMethod.getParameterCount() != 1) {
            throw new RuntimeException(
                    "The parameter found is supposed to be 1, but it was" + applyMethod.getParameterCount()
            );
        }
        return applyMethod.getParameterTypes()[0];
    }

    private Method getMethod() {

        Optional<Method> foundMethod = Arrays.stream(functionObject.getClass().getMethods())
                .filter(m -> !m.isSynthetic() && !m.isBridge() && !m.isDefault())
                .filter(m -> m.getName().startsWith("apply"))
                .findFirst();

        return foundMethod.orElseThrow(
                () -> new RuntimeException(
                        "Unable to find the function method on " + functionObject.getClass().getCanonicalName()
                )
        );
    }

    public boolean isThreadSafe() {
        return isThreadSafe;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (libraryName != null) {
            sb.append(libraryName).append("::");
        }
        sb.append(getArgType().getSimpleName()).append("->");
        sb.append(getMethod().getDeclaringClass().getName());
//        sb.append(getFunctionObject().getClass().getPackage().getLibname()).append(".").append(getMethod().getLibname());
        sb.append("->").append(getResultClass().getName());

        if (initializerValues != null && initializerValues.length > 0) {
            sb.append(" [");
            for (int i = 0; i < initializerSignature.length; i++) {
                Class<?> isig = initializerSignature[i];
                String assignToType = isig.isPrimitive() ? isig.getName() : isig.getSimpleName();

                String assignFromType = "[]";
                if (i < initializerValues.length) {
                    Class<?> init = initializerValues[i].getClass();
                    assignFromType = init.isPrimitive() ? init.getName() : init.getSimpleName();
                }

                if (initializerSignature.length != initializerValues.length && i == initializerSignature.length - 1) {
                    assignToType = assignToType.replaceAll("\\[]", "") + "...";
                    assignFromType = assignFromType.replaceAll("\\[]", "") + "...";
                }
                sb.append(assignFromType).append("=>").append(assignToType);
                sb.append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append("]");

        }
        return sb.toString();
    }

    /**
     * Compare two ResolvedFunctions by preferred input type and then by preferred output type.
     */
    private static class PreferredTypeComparator implements Comparator<ResolvedFunction> {

        @Override
        public int compare(ResolvedFunction o1, ResolvedFunction o2) {
            ValueType iv1 = ValueType.valueOfAssignableClass(o1.getArgType());
            ValueType iv2 = ValueType.valueOfAssignableClass(o2.getArgType());
            int inputComparison = iv1.compareTo(iv2);
            if (inputComparison != 0) {
                return inputComparison;
            }
            iv1 = ValueType.valueOfAssignableClass(o1.getResultClass());
            iv2 = ValueType.valueOfAssignableClass(o2.getResultClass());
            return iv1.compareTo(iv2);
        }
    }


}
