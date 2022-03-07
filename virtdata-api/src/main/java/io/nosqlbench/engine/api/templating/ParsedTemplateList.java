package io.nosqlbench.engine.api.templating;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class ParsedTemplateList implements LongFunction<List<?>> {
    private final List<Object> protolist = new ArrayList<>();
    private final int[] dynamic_idx;
    private final LongFunction<?>[] functions;

    public ParsedTemplateList(List<Object> sublist, Map<String, String> bindings, List<Map<String, Object>> cfgsources) {

        List<LongFunction<?>> funcs = new ArrayList<>();
        List<Integer> dindexes = new ArrayList<>();

        for (int i = 0; i < sublist.size(); i++) {
            Object item = sublist.get(i);
            Templatizer.Result result = Templatizer.make(bindings, item, null, cfgsources);
            switch (result.getType()) {
                case literal:
                    protolist.add(result.getValue());
                    break;
                case bindref:
                case concat:
                    protolist.add(null);
                    funcs.add(result.getFunction());
                    dindexes.add(i);
                    break;
            }
        }
        this.dynamic_idx = dindexes.stream().mapToInt(Integer::intValue).toArray();
        this.functions = funcs.toArray(new LongFunction<?>[0]);

    }

    @Override
    public List<?> apply(long value) {
        List<Object> list = new ArrayList<>(protolist);
        for (int i = 0; i < dynamic_idx.length; i++) {
            Object obj = functions[i].apply(value);
            list.set(dynamic_idx[i], obj);
        }
        return list;
    }

    public boolean isStatic() {
        return dynamic_idx.length==0;
    }
}
