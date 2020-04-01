package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.DataMapperFunctionMapper;
import io.nosqlbench.virtdata.core.bindings.ResolvedFunction;

public interface FunctionComposer<T> {

    Object getFunctionObject();

    FunctionComposer andThen(Object outer);

    default ResolvedFunction getResolvedFunction() {
        return new ResolvedFunction(
                getFunctionObject(),
                getFunctionObject().getClass().getAnnotation(ThreadSafeMapper.class) != null,
                null, null,
                null, null
        );
    }

    default ResolvedFunction getResolvedFunction(boolean isThreadSafe) {
        return new ResolvedFunction(
                getFunctionObject(),
                isThreadSafe,
                null, null,
                null, null
        );
    }

    default <R> DataMapper<R> getDataMapper() {
        return DataMapperFunctionMapper.map(getFunctionObject());
    }

}
