package io.virtdata.api.composers;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.api.DataMapper;
import io.virtdata.core.DataMapperFunctionMapper;
import io.virtdata.core.ResolvedFunction;

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
