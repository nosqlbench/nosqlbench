package io.nosqlbench.virtdata.api;

import io.nosqlbench.virtdata.lang.ast.Expression;
import io.nosqlbench.virtdata.lang.ast.FunctionCall;
import io.nosqlbench.virtdata.lang.parser.VirtDataDSL;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A VirtDataFunctionLibrary is simply a way to ask for a set
 * of named function objects in a generic way.
 */
public interface VirtDataFunctionLibrary extends Named {

    /**
     * Given a signature for a unary function which takes an input
     * and output type, a function name, and constructor arguments,
     * return a list of instances from all implementations that have
     * the same name as the function name,
     * which have a matching constructor signature, and which also
     * have a functional method which can be used with the provided
     * input and output types.
     *
     * The input and output types are optionally specified. If either
     * is provided, the returned functions should be constrained to match,
     * but otherwise all possibly matching functions are included.
     *
     * Further, the argument should not be strict type checks, but should
     * allow any matching constructor for which a compatible assignment
     * can be made.
     *
     * The specified function name does not have to map to a
     *
     * @param returnType     The class which the apply method should return,
     *                       or null if unspecified
     * @param inputType      The class which the unary apply method should take as an
     *                       argument, or null if unspecified
     * @param functionName   The name of the implementation to match
     * @param customConfigs  Optional initializer configuration
     * @param parameters A list of arguments which will be used to instantiate
     *                       any matching implementations
     * @return A list, possibly empty, of matching functions
     */
    List<ResolvedFunction> resolveFunctions(
            Class<?> returnType,
            Class<?> inputType,
            String functionName,
            Map<String,?> customConfigs,
            Object... parameters
            );


    default List<ResolvedFunction> resolveFunction(String spec) {
        return this.resolveFunctions(spec, new HashMap<>());
    }
    default List<ResolvedFunction> resolveFunctions(String spec, Map<String,Object> customConfigs) {
        List<ResolvedFunction> resolvedFunctions = new ArrayList<>();

        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(spec);
        if (parseResult.throwable!=null) {
            throw new RuntimeException(parseResult.throwable);
        }

        List<Expression> expressions = parseResult.flow.getExpressions();
        if (expressions.size() > 1) {
            throw new RuntimeException("Unable to promote a lambda flow to a data mapper here.");
        }
        FunctionCall call = expressions.get(0).getCall();

        List<ResolvedFunction> found = resolveFunctions(
                Optional.ofNullable(call.getOutputType()).map(ValueType::valueOfClassName).map(ValueType::getValueClass).orElse(null),
                Optional.ofNullable(call.getInputType()).map(ValueType::valueOfClassName).map(ValueType::getValueClass).orElse(null),
                call.getFunctionName(),
                customConfigs,
                call.getArguments());

        resolvedFunctions.addAll(found);
        return resolvedFunctions;
    }

    default <T> List<DataMapper<T>> getDataMappers(String spec) {
        return this.getDataMappers(spec,new HashMap<>());
    }
    default <T> List<DataMapper<T>> getDataMappers(String spec, Map<String,Object> customConfigs) {
        List<ResolvedFunction> resolvedFunctions1 = this.resolveFunctions(spec, customConfigs);
        return resolvedFunctions1.stream().map(
                r -> DataMapperFunctionMapper.<T>map(r.getFunctionObject())).collect(Collectors.toList());
    }


    /**
     * Provide a way to promote a long function into a data mapper.
     *
     * @param spec a binding spec
     * @param <T> The type of data mapper to return
     * @return An optional data mapper
     */
    default <T> Optional<DataMapper<T>> getDataMapper(String spec) {
        return this.getDataMapper(spec, new HashMap<>());
    }
    default <T> Optional<DataMapper<T>> getDataMapper(String spec, Map<String,Object> customConfigs) {
        List<ResolvedFunction> resolvedFunctions = this.resolveFunctions(spec, customConfigs);

        switch (resolvedFunctions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(DataMapperFunctionMapper.<T>map(resolvedFunctions.get(0).getFunctionObject()));
            default:
                throw new RuntimeException(
                        "Found " + resolvedFunctions.size() +
                                " data mapping functions, expected exactly one for library-level function lookups." +
                                " This may require both an input and an output type qualifier like 'int -> f() -> int'." +
                                " \nFound: [<library name>::] input->class->output [initializer type->parameter type,...]: \n" +
                                resolvedFunctions.stream().map(String::valueOf).collect(Collectors.joining("\n")));

        }
    }

}
