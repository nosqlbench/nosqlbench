package io.nosqlbench.engine.api.templating.binders;

import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import io.nosqlbench.virtdata.core.templates.BindPoint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class ArrayBinder implements LongFunction<Object[]> {

    private final Object[] protoary;
    private final LongFunction<?>[] mapperary;
    private final int[] dindexes;

    public ArrayBinder(ParsedCommand cmd, String[] fields) {
        this.protoary = new Object[fields.length];
        this.mapperary = new LongFunction<?>[fields.length];
        int[] indexes = new int[fields.length];
        int nextIndex = 0;

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (cmd.isDefinedStatic(field)) {
                protoary[i] = cmd.getStaticValue(field);
            } else if (cmd.isDefinedDynamic(field)) {
                mapperary[i] = cmd.getMapper(field);
                indexes[nextIndex++] = i;
            } else {
                throw new OpConfigError("There was no field named '" + field + "' while building an ArrayBinder.");
            }
        }
        this.dindexes = Arrays.copyOf(indexes, nextIndex);
    }

    public ArrayBinder(ParsedCommand cmd, List<String> fields) {
        this(cmd, fields.toArray(new String[0]));
    }

    public ArrayBinder(List<BindPoint> bindPoints) {
        this.protoary = new Object[bindPoints.size()];
        this.mapperary = new LongFunction<?>[bindPoints.size()];
        int[] indexes = new int[bindPoints.size()];
        int nextIndex = 0;

        for (int i = 0; i < bindPoints.size(); i++) {
            BindPoint bindPoint = bindPoints.get(i);
            Optional<DataMapper<Object>> mapper = VirtData.getOptionalMapper(bindPoint.getBindspec());
            mapperary[i] = mapper.orElseThrow();
            indexes[nextIndex++] = i;
        }
        this.dindexes = Arrays.copyOf(indexes, nextIndex);

    }

    @Override
    public Object[] apply(long value) {
        Object[] ary = Arrays.copyOf(protoary, protoary.length);
        for (int dindex : this.dindexes) {
            ary[dindex] = this.mapperary[dindex].apply(value);
        }
        return ary;
    }
}
