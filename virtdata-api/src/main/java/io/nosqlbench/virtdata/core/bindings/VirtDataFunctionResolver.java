package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.nb.api.config.ConfigAware;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class VirtDataFunctionResolver {
    private final static Logger logger  = LogManager.getLogger(VirtDataFunctionResolver.class);
    private final static MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    private final VirtDataFunctionFinder virtDataFunctionFinder = new VirtDataFunctionFinder();

    public List<ResolvedFunction> resolveFunctions(Class<?> returnType, Class<?> inputType, String functionName, Map<String,?> customParameters, Object... parameters) {

        // TODO: Make this look for both assignment compatible matches as well as exact assignment matches, and only
        // TODO: return assignment compatible matches when there are none exact matching.
        // TODO: Further, make lambda construction honor exact matches first as well.

        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }

        List<ResolvedFunction> resolvedFunctions = new ArrayList<>();

        List<Class<?>> matchingClasses = virtDataFunctionFinder.getFunctionNames()
                .stream()
                .filter(s -> s.endsWith("." + functionName))
                .map(this::maybeClassForName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Constructor<?>> matchingConstructors = null;
        matchingConstructors = matchingClasses.stream()
                .filter(c -> {
                    // This form for debugging
                    boolean isFunctional = isFunctionalInterface(c);
                    boolean canAssignInput = inputType == null || canAssignInputType(c, inputType);
                    boolean canAssignReturn = returnType == null || canAssignReturnType(c, returnType);
                    boolean matchesSignature = isFunctional && canAssignInput && canAssignReturn;
                    return matchesSignature;
                })
                .flatMap(c -> Arrays.stream(c.getDeclaredConstructors()))
                .filter(c -> {
                    Class<?>[] ctypes = c.getParameterTypes();
                    if (c.isVarArgs()) {
                        int commonLen=Math.min(ctypes.length-1,parameters.length);
                        Class<?>[] paramNonVarArgs = Arrays.copyOfRange(parameterTypes, 0, commonLen);
                        Class<?>[] ctorNonVarArgs = Arrays.copyOfRange(ctypes, 0, commonLen);
                        if (parameters.length< (ctypes).length-1) {
                            return false;
                        }
                        if (!ClassUtils.isAssignable(paramNonVarArgs, ctorNonVarArgs, true)) {
                            return false;
                        }
                        Class<?> componentType = ctypes[ctypes.length - 1].getComponentType();
                        return parameterTypes.length < ctypes.length || ClassUtils.isAssignable(parameterTypes[ctypes.length - 1], componentType, true);
                    } else {
                        if (parameterTypes.length!=ctypes.length) {
                            return false;
                        }
                        return ClassUtils.isAssignable(parameterTypes, ctypes, true);
                    }
                })
//                .map(c -> {
//                    try {
//                        return lookup.findConstructor(c, MethodType.methodType(void.class, parameterTypes));
//                    } catch (NoSuchMethodException | IllegalAccessException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
                .collect(Collectors.toList());

        if (returnType != null && inputType != null && matchingConstructors.size() > 1) {
            throw new RuntimeException(
                    "found more than one (" + matchingConstructors.size() + ") matching constructor for " +
                            "return type '" + returnType + "', " +
                            "inputType '" + inputType + "', " +
                            "function name '" + functionName + ", " +
                            "and parameter types '" + Arrays.toString(parameters) + "', " +
                            "ctors: " + matchingConstructors);

        }

        for (Constructor<?> ctor : matchingConstructors) {
            try {
                Class<?> ctorDClass = ctor.getDeclaringClass();
                MethodType ctorMethodType = MethodType.methodType(void.class, ctor.getParameterTypes());
                MethodHandle constructor = lookup.findConstructor(ctorDClass, ctorMethodType);
                Object functionalInstance = constructor.invokeWithArguments(parameters);
                if (functionalInstance instanceof ConfigAware) {
                    ((ConfigAware)functionalInstance).applyConfig(customParameters);
                }
                boolean threadSafe = functionalInstance.getClass().getAnnotation(ThreadSafeMapper.class) != null;
                resolvedFunctions.add(
                        new ResolvedFunction(
                                functionalInstance,
                                threadSafe,
                                parameterTypes,
                                parameters,
                                getInputClass(functionalInstance.getClass()),
                                getOutputClass(functionalInstance.getClass())
                        )
                );
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
        return resolvedFunctions;
    }

    private boolean isFunctionalInterface(Class<?> c) {
        Optional<Method> applyMethods = Arrays.stream(c.getMethods())
                .filter(m -> {
                    boolean isNotDefault = !m.isDefault();
                    boolean isNotBridge = !m.isBridge();
                    boolean isNotSynthetic = !m.isSynthetic();
                    boolean isPublic = (m.getModifiers() & Modifier.PUBLIC) > 0;
                    boolean isNotString = !m.getName().equals("toString");
                    boolean isApplyMethod = m.getName().startsWith("apply");
                    boolean isFunctional = isNotDefault && isNotBridge && isNotSynthetic && isPublic && isNotString && isApplyMethod;
                    return isFunctional;
                })
                .findFirst();
        return applyMethods.isPresent();
    }

    private boolean canAssignArguments(Constructor<?> targetCtor, Object[] sourceParameters) {
        boolean isAssignable = true;
        Class<?>[] targetTypes = targetCtor.getParameterTypes();

        if (targetCtor.isVarArgs()) {
            if (sourceParameters.length < (targetTypes.length - 1)) {
                logger.trace(targetCtor.toString() + " (varargs) does not match, not enough source parameters: " + Arrays.toString(sourceParameters));
                return false;
            }
        } else if (sourceParameters.length != targetTypes.length) {
            logger.trace(targetCtor.toString() + " (varargs) does not match source parameters (size): " + Arrays.toString(sourceParameters));
            return false;
        }

        Class<?>[] sourceTypes = new Class<?>[sourceParameters.length];
        for (int i = 0; i < sourceTypes.length; i++) {
            sourceTypes[i] = sourceParameters[i].getClass();
        }

        if (targetCtor.isVarArgs()) {
            for (int i = 0; i < targetTypes.length - 1; i++) {
                if (!ClassUtils.isAssignable(sourceTypes[i], targetTypes[i])) {
                    isAssignable = false;
                    break;
                }
            }
            Class<?> componentType = targetTypes[targetTypes.length - 1].getComponentType();
            for (int i = targetTypes.length - 1; i < sourceTypes.length; i++) {
                if (!ClassUtils.isAssignable(sourceTypes[i], componentType, true)) {

                    isAssignable = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < targetTypes.length; i++) {
                if (!ClassUtils.isAssignable(sourceTypes[i], targetTypes[i])) {
                    isAssignable = false;
                    break;
                }
            }
//
//            isAssignable = ClassUtils.isAssignable(sourceTypes, targetTypes, true);
        }
        return isAssignable;
    }

    private boolean canAssignReturnType(Class<?> functionalClass, Class<?> returnType) {
        Class<?> sourceType = toFunctionalMethod(functionalClass).getReturnType();
        boolean isAssignable = returnType.isAssignableFrom(sourceType);
        return isAssignable;
    }

    private Class<?> getInputClass(Class<?> functionalClass) {
        return toFunctionalMethod(functionalClass).getParameterTypes()[0];
    }

    private Class<?> getOutputClass(Class<?> functionClass) {
        return toFunctionalMethod(functionClass).getReturnType();
    }

    private boolean canAssignInputType(Class<?> functionalClass, Class<?> inputType) {
        boolean isAssignable = toFunctionalMethod(functionalClass).getParameterTypes()[0].isAssignableFrom(inputType);
        return isAssignable;
    }

    private Class<?> maybeClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            return null;
        }
    }

    private Method toFunctionalMethod(Class<?> clazz) {

        Optional<Method> foundMethod = Arrays.stream(clazz.getMethods())
                .filter(m -> !m.isDefault() && !m.isBridge() && !m.isSynthetic())
                .filter(m -> m.getName().startsWith("apply"))
                .findFirst();

        return foundMethod.orElseThrow(
                () -> new RuntimeException(
                        "Unable to find the function method on " + clazz.getCanonicalName()
                )
        );
    }

    public List<String> getFunctionNames() {
        return virtDataFunctionFinder.getFunctionNames();
    }


//    public List<DocFuncData> getDocModels() {
//        List<String> classes= virtDataFunctionFinder.getFunctionNames().stream().map(s -> s+"DocInfo").collect(Collectors.toList());
//        List<DocFuncData> docFuncData = new ArrayList<>(classes.size());
//        for (String aClass : classes) {
//            try {
//                Class<?> docClass = Class.forName(aClass);
//                if (DocFuncData.class.isAssignableFrom(docClass)) {
//                    DocFuncData dfd = DocFuncData.class.cast(docClass);
//                    docFuncData.add(dfd);
//                } else {
//                    throw new RuntimeException("Unable to cast " + docClass.getCanonicalName() + " to class DocFuncData");
//                }
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return docFuncData;
//    }

}
