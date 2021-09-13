package io.nosqlbench.engine.api.templating.binders;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class OrderedMapBinder implements LongFunction<Map<String, Object>> {

    private final Map<String,Object> protomap = new LinkedHashMap<>();
    private final Map<String,LongFunction<?>> bindermap = new HashMap<>();

    public OrderedMapBinder(ParsedOp cmd, String... fields) {
        for (String field : fields) {
            if (cmd.isStatic(field)) {
                protomap.put(field,cmd.getStaticValue(field));
            } else if (cmd.isDefinedDynamic(field)) {
                bindermap.put(field,cmd.getMapper(field));
                protomap.put(field,null);
            } else {
                throw new OpConfigError("There was no field named " + field + " while building a MapBinder");
            }
        }
    }

    @Override
    public Map<String, Object> apply(long value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(protomap);
        bindermap.forEach((k,v) -> {
            map.put(k,v.apply(value));
        });
        return map;
    }
}
