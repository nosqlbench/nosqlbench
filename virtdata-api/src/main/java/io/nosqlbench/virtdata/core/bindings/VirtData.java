package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.lang.ast.VirtDataFlow;
import io.nosqlbench.virtdata.lang.parser.VirtDataDSL;
import io.nosqlbench.virtdata.core.templates.BindPoint;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class VirtData {
    private final static Logger logger  = LogManager.getLogger(VirtData.class);
    /**
     * Create a bindings template from the pair-wise names and specifiers.
     * Each even-numbered (starting with zero) argument is a binding name,
     * and each odd-numbered (starting with one) argument is a binding spec.
     *
     * @param namesAndSpecs names and specs in "name", "spec", ... form
     * @return A bindings template that can be used to resolve a bindings instance
     */
    public static BindingsTemplate getTemplate(Map<String,Object> config, String... namesAndSpecs) {
        if ((namesAndSpecs.length % 2) != 0) {
            throw new RuntimeException(
                    "args must be in 'name','spec', pairs. " +
                            "This can't be true for " + namesAndSpecs.length + "elements.");
        }
        List<BindPoint> bindPoints = new ArrayList<>();
        for (int i = 0; i < namesAndSpecs.length; i += 2) {
            bindPoints.add(new BindPoint(namesAndSpecs[i],namesAndSpecs[i+1]));
        }
        return getTemplate(config, bindPoints);
    }

    public static BindingsTemplate getTemplate(String... namesAndSpecs) {
        return getTemplate(Map.of(), namesAndSpecs);
    }

//    /**
//     * Create a bindings template from the provided map, ensuring that
//     * the syntax of the bindings specs is parsable first.
//     *
//     * @param namedBindings The named bindings map
//     * @return a bindings template
//     */
//    public static BindingsTemplate getTemplate(Map<String, String> namedBindings) {
//
//        for (String bindingSpec : namedBindings.values()) {
//            VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(bindingSpec);
//            if (parseResult.throwable != null) {
//                throw new RuntimeException(parseResult.throwable);
//            }
//        }
//        return new BindingsTemplate(namedBindings);
//    }

    /**
     * Create a bindings template from a provided list of {@link BindPoint}s,
     * ensuring that the syntax of the bindings specs is parsable first.
     *
     * @param bindPoints A list of {@link BindPoint}s
     * @return A BindingsTemplate
     */
    public static BindingsTemplate getTemplate(Map<String,Object> config, List<BindPoint> bindPoints) {
        for (BindPoint bindPoint : bindPoints) {
            String bindspec = bindPoint.getBindspec();
            VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(bindspec);
            if (parseResult.throwable!=null) {
                throw new RuntimeException(parseResult.throwable);
            }
        }
        return new BindingsTemplate(config, bindPoints);
    }

    /**
     * Instantiate an optional data mapping function if possible.
     *
     * @param flowSpec The VirtData specifier for the mapping function
     * @param config   A map of configuration objects
     * @param <T>      The parameterized return type of the function
     * @return An optional function which will be empty if the function could not be resolved.
     */
    public static <T> Optional<DataMapper<T>> getOptionalMapper(String flowSpec, Map<String,?> config) {
        flowSpec = CompatibilityFixups.fixup(flowSpec);
        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(flowSpec);
        if (parseResult.throwable != null) {
            throw new RuntimeException(parseResult.throwable);
        }
        VirtDataFlow flow = parseResult.flow;
        VirtDataComposer composer = new VirtDataComposer();
        composer.addCustomElements(config);
        Optional<ResolvedFunction> resolvedFunction = composer.resolveFunctionFlow(flow);
        return resolvedFunction.map(ResolvedFunction::getFunctionObject).map(DataMapperFunctionMapper::map);
    }
    public static <T> Optional<DataMapper<T>> getOptionalMapper(String flowSpec) {
        return getOptionalMapper(flowSpec,Collections.emptyMap());
    }

    public static ResolverDiagnostics getMapperDiagnostics(String flowSpec) {
        return getMapperDiagnostics(flowSpec, Collections.emptyMap());
    }

    public static ResolverDiagnostics getMapperDiagnostics(String flowSpec, Map<String,Object> config) {
        try {
            flowSpec = CompatibilityFixups.fixup(flowSpec);
            VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(flowSpec);
            if (parseResult.throwable != null) {
                throw new RuntimeException(parseResult.throwable);
            }
            VirtDataFlow flow = parseResult.flow;
            VirtDataComposer composer = new VirtDataComposer();
            composer.addCustomElements(config);

            ResolverDiagnostics resolverDiagnostics = composer.resolveDiagnosticFunctionFlow(flow);
            return resolverDiagnostics;
        } catch (Exception e) {
            return new ResolverDiagnostics().error(e);
        }
    }

    /**
     * Instantiate an optional data mapping function if possible, with type awareness. This version
     * of {@link #getOptionalMapper(String)} will use the additional type information in the clazz
     * parameter to automatically parameterize the flow specifier.
     *
     * If the flow specifier does contain
     * an output type qualifier already, then a check is made to ensure that the output type qualifier is
     * assignable to the specified class in the clazz parameter. This ensures that type parameter awareness
     * at compile time is honored and verified when this call is made.
     *
     * @param flowSpec The VirtData specifier for the mapping function
     * @param <T>      The parameterized return type of the function.
     * @param clazz    The explicit class which must be of type T or assignable to type T
     * @return An optional function which will be empty if the function could not be resolved.
     */
    public static <T> Optional<DataMapper<T>> getOptionalMapper(String flowSpec, Class<? extends T> clazz) {
        return getOptionalMapper(flowSpec,clazz,Collections.emptyMap());
    }
    public static <T> Optional<DataMapper<T>> getOptionalMapper(
            String flowSpec,
            Class<?> clazz,
            Map<String,Object> config) {
        flowSpec = CompatibilityFixups.fixup(flowSpec);
        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(flowSpec);
        if (parseResult.throwable != null) {
            throw new RuntimeException(parseResult.throwable);
        }
        VirtDataFlow flow = parseResult.flow;
        String outputType = flow.getLastExpression().getCall().getOutputType();

        Class<?> outputClass = ValueType.classOfType(outputType);
        if (outputClass != null) {
            if (!ClassUtils.isAssignable(outputClass,clazz,true)) {
                throw new RuntimeException("The flow specifier '" + flowSpec + "' wants an output type of '" + outputType +"', but this" +
                        " type is not assignable to the explicit class '" + clazz.getCanonicalName() + "' that was enforced at the API level." +
                        " Either remove the output type qualifier at the last function in the flow spec, or change it to something that can" +
                        " reliably be cast to type '" + clazz.getCanonicalName() +"'");
            }
        } else {
            logger.debug("Auto-assigning output type qualifier '->" + clazz.getCanonicalName() + "' to specifier '" + flowSpec + "'");
            flow.getLastExpression().getCall().setOutputType(clazz.getCanonicalName());
        }

        VirtDataComposer composer = new VirtDataComposer();
        composer.addCustomElements(config);
        Optional<ResolvedFunction> resolvedFunction = composer.resolveFunctionFlow(flow);
        Optional<DataMapper<T>> mapper = resolvedFunction.map(ResolvedFunction::getFunctionObject).map(DataMapperFunctionMapper::map);
        if (mapper.isPresent()) {
            T actualTestValue = mapper.get().get(1L);
            if (!ClassUtils.isAssignable(actualTestValue.getClass(),clazz,true)) {
                throw new RuntimeException("The flow specifier '" + flowSpec + "' successfully created a function, but the test value" +
                        "(" + String.valueOf(actualTestValue) + ") of type [" + actualTestValue.getClass() + "] produced by it was not " +
                        "assignable to the type '" + clazz.getCanonicalName() + "' which was explicitly set" +
                        " at the API level.");
            }
        }
        return mapper;
    }

    public static <T> T getFunction(String flowSpec, Class<? extends T> functionType) {
            return getFunction(flowSpec, functionType, Collections.emptyMap());
    }

    public static <T> T getFunction(String flowSpec, Class<? extends T> functionType, Map<String,Object> config) {
        Optional<? extends T> optionalFunction = getOptionalFunction(flowSpec, functionType, config);
        return optionalFunction.orElseThrow();
    }

    public static <T> Optional<T> getOptionalFunction(String flowSpec, Class<? extends T> functionType) {
        return getOptionalFunction(flowSpec,functionType,Collections.emptyMap());
    }

    public static <T> Optional<T> getOptionalFunction(String flowSpec, Class<? extends T> functionType, Map<String,Object> config) {
        flowSpec = CompatibilityFixups.fixup(flowSpec);

        Class<?> requiredInputType = FunctionTyper.getInputClass(functionType);
        Class<?> requiredOutputType = FunctionTyper.getResultClass(functionType);

        FunctionalInterface annotation = functionType.getAnnotation(FunctionalInterface.class);
        if (annotation==null) {
            throw new RuntimeException("You can only use function types that are tagged as @FunctionInterface");
        }

        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(flowSpec);
        if (parseResult.throwable != null) {
            throw new RuntimeException(parseResult.throwable);
        }
        VirtDataFlow flow = parseResult.flow;

        String specifiedInputClassName = flow.getFirstExpression().getCall().getInputType();
        Class<?> specifiedInputClass = ValueType.classOfType(specifiedInputClassName);
        if (specifiedInputClass!=null) {
            if (!ClassUtils.isAssignable(specifiedInputClass,requiredInputType,true)) {
                throw new RuntimeException("The flow specifier '" + flowSpec + "' wants an input type of '" + specifiedInputClassName +"', but this" +
                        " type is not assignable to the input class required by the functional type requested '" + functionType.getCanonicalName() + "'. (type "+requiredInputType.getCanonicalName()+")" +
                        " Either remove the input type qualifier at the first function in the flow spec, or change it to something that can" +
                        " reliably be cast to type '" + requiredInputType.getCanonicalName() +"'");

            }
        } else {
            logger.debug("Auto-assigning input type qualifier '" + requiredInputType.getCanonicalName()  + "->' to specifier '" + flowSpec + "'");
            flow.getFirstExpression().getCall().setInputType(requiredInputType.getCanonicalName());
        }

        String specifiedOutputClassName = flow.getLastExpression().getCall().getOutputType();
        Class<?> specifiedOutputClass = ValueType.classOfType(specifiedOutputClassName);
        if (specifiedOutputClass != null) {
            if (!ClassUtils.isAssignable(specifiedOutputClass,requiredOutputType,true)) {
                throw new RuntimeException("The flow specifier '" + flowSpec + "' wants an output type of '" + specifiedOutputClass +"', but this" +
                        " type is not assignable to the output class required by functional type '" + functionType.getCanonicalName() + "'. (type "+requiredOutputType.getCanonicalName()+")" +
                        " Either remove the output type qualifier at the last function in the flow spec, or change it to something that can" +
                        " reliably be cast to type '" + requiredOutputType.getCanonicalName() +"'");
            }
        } else {
            logger.debug("Auto-assigning output type qualifier '->" + requiredOutputType.getCanonicalName() + "' to specifier '" + flowSpec + "'");
            flow.getLastExpression().getCall().setOutputType(requiredOutputType.getCanonicalName());
        }

        VirtDataComposer composer = new VirtDataComposer();
        composer.addCustomElements(config);
        Optional<ResolvedFunction> resolvedFunction = composer.resolveFunctionFlow(flow);

        return resolvedFunction.map(ResolvedFunction::getFunctionObject).map(functionType::cast);
    }

    /**
     * Instantiate a data mapping function, or throw an exception.
     *
     * @param flowSpec The VirtData specifier for the mapping function
     * @param <T>      The parameterized return type of the function
     * @param config   A map of configuration objects
     * @return A data mapping function
     * @throws RuntimeException if the function could not be resolved
     */
    public static <T> DataMapper<T> getMapper(String flowSpec, Map<String,Object> config) {
        Optional<DataMapper<T>> optionalMapper = getOptionalMapper(flowSpec,config);
        return optionalMapper.orElseThrow(() -> new RuntimeException("Unable to find mapper: " + flowSpec));
    }
    public static <T> DataMapper<T> getMapper(String flowSpec) {
        return getMapper(flowSpec, Collections.emptyMap());
    }

    /**
     * Instantiate a data mapping function of the specified type, or throw an error.
     *
     * @param flowSpec The VirtData flow specifier for the function to be returned
     * @param clazz    The class of the data mapping function return type
     * @param <T>      The parameterized class of the data mapping return type
     * @param config   A map of configuration objects
     * @return A new data mapping function.
     * @throws RuntimeException if the function could not be resolved
     */
    public static <T> DataMapper<T> getMapper(String flowSpec, Class<? extends T> clazz, Map<String,Object> config) {
        Optional<DataMapper<T>> dataMapper = getOptionalMapper(flowSpec, clazz);
        DataMapper<T> mapper = dataMapper.orElseThrow(() -> new RuntimeException("Unable to find mapper: " + flowSpec));
        return mapper;
    }
    public static <T> DataMapper<T> getMapper(String flowSpec, Class<? extends T> clazz) {
        return getMapper(flowSpec, clazz, Collections.emptyMap());
    }

}
