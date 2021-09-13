    package io.nosqlbench.engine.api.templating.binders;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class ListBinder implements LongFunction<List<Object>> {

    private final ArrayList<Object> protolist;
    private final ArrayList<LongFunction<?>> mapperlist;
    private final int[] dindexes;

    public ListBinder(ParsedOp cmd, String... fields) {
        this.protolist = new ArrayList<>(fields.length);
        this.mapperlist = new ArrayList<>(fields.length);
        int[] indexes = new int[fields.length];
        int lastIndex = 0;

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (cmd.isStatic(field)) {
                protolist.add(cmd.getStaticValue(field));
                mapperlist.add(null);
            } else if (cmd.isDefinedDynamic(field)) {
                protolist.add(null);
                mapperlist.add(cmd.getMapper(field));
                indexes[lastIndex++]=i;
            } else {
                throw new OpConfigError("No defined field '" + field + "' when creating list binder");
            }
        }
        this.dindexes = Arrays.copyOf(indexes,lastIndex);
    }

    public ListBinder(ParsedOp cmd, List<String> fields) {
        this(cmd,fields.toArray(new String[0]));
    }

    @Override
    public List<Object> apply(long value) {
        ArrayList<Object> list = new ArrayList<>(protolist);
        for (int index : this.dindexes) {
            list.set(index,mapperlist.get(index).apply(value));
        }
        return list;
    }
}
