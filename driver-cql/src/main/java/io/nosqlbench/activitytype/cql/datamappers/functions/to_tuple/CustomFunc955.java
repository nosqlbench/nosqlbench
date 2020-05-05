package io.nosqlbench.activitytype.cql.datamappers.functions.to_tuple;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.config.ConfigAware;
import io.nosqlbench.virtdata.core.config.ConfigModel;
import io.nosqlbench.virtdata.core.config.MutableConfigModel;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;

/**
 * Temporary function to test a specific nested type. This should be replaced
 * with a general custom/tuple type aware binding function.
 * The type supported is a CQL type: {@code map<text, frozen<tuple<int, bigint>>}
 *
 * Functions are required for:
 * <LI>
 *     <LI>map size {@code (LongToIntFunction)}</LI>
 *     <LI>key {@code (LongFunction<Object>)}</LI>
 *     <LI>tuple field 1 {@code (LongToIntFunction)}</LI>
 *     <LI>tuple field 2 {@code {LongToIntFunction)}</LI>
 * </LI>
 */
@ThreadSafeMapper
public class CustomFunc955 implements LongFunction<Map<?,?>>, ConfigAware {

    private final LongToIntFunction sizefunc;
    private final LongFunction<Object> keyfunc;
    private final LongToIntFunction field1func;
    private final LongUnaryOperator field2func;
    private Cluster cluster;
    private TupleType tupleType;

    public CustomFunc955(LongToIntFunction sizefunc, LongFunction<Object> keyfunc,
                         LongToIntFunction field1func, LongToIntFunction field2func) {

        this.sizefunc = sizefunc;
        this.keyfunc = keyfunc;
        this.field1func = field1func;
        this.field2func = field2func::applyAsInt;
    }

    public CustomFunc955(LongToIntFunction sizefunc, LongFunction<Object> keyfunc,
                         LongToIntFunction field1func, LongUnaryOperator field2func) {

        this.sizefunc = sizefunc;
        this.keyfunc = keyfunc;
        this.field1func = field1func;
        this.field2func = field2func;
    }

    @Override
    public Map<?,?> apply(long value) {
        int size = sizefunc.applyAsInt(value);

        HashMap<String, TupleValue> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = keyfunc.apply(value+i).toString();
            int tuple1 = field1func.applyAsInt(value+i);
            long tuple2 = field2func.applyAsLong(value+i);
            TupleValue tupleValue = tupleType.newValue(tuple1, tuple2);
            map.put(key,tupleValue);
        }
        return map;
    }

    @Override
    public void applyConfig(Map<String, ?> elements) {
        this.cluster = Optional.ofNullable(elements.get("cluster"))
            .map(Cluster.class::cast)
            .orElseThrow();
        this.tupleType = cluster.getMetadata().newTupleType(DataType.cint(), DataType.bigint());
    }

    @Override
    public ConfigModel getConfigModel() {
        return new MutableConfigModel()
            .add("<cluster>", Cluster.class)
            .asReadOnly();
    }
}
