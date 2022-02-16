package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * This implementation of a string compositor takes a logically coherent
 * string template and bindings set. It employs a few simplistic optimizations
 * to avoid re-generating duplicate values, as well as lower allocation
 * rate of buffer data.
 */
public class StringCompositor implements LongFunction<String> {

    private final String[] spans;
    private final DataMapper<?>[] mappers;
    private final int[] LUT;
    private final int bufsize;

    private final Function<Object, String> stringfunc;

    public StringCompositor(ParsedTemplate template, Map<String,Object> fconfig, Function<Object,String> stringfunc) {
        Map<String,Integer> specs = new HashMap<>();
        List<BindPoint> bindpoints = template.getBindPoints();
        for (BindPoint bindPoint : bindpoints) {
            String spec = bindPoint.getBindspec();
            specs.compute(spec,(s,i) -> i==null ? specs.size() : i);
        }
        mappers = new DataMapper<?>[specs.size()];
        specs.forEach((k,v) -> {
            mappers[v]= VirtData.getOptionalMapper(k,fconfig).orElseThrow();
        });
        String[] even_odd_spans = template.getSpans();
        this.spans = new String[bindpoints.size()+1];
        LUT = new int[bindpoints.size()];
        for (int i = 0; i < bindpoints.size(); i++) {
            spans[i]=even_odd_spans[i<<1];
            LUT[i]=specs.get(template.getBindPoints().get(i).getBindspec());
        }
        spans[spans.length-1]=even_odd_spans[even_odd_spans.length-1];
        this.stringfunc = stringfunc;

        int minsize = 0;
        for (int i = 0; i < 100; i++) {
            String result = apply(i);
            minsize = Math.max(minsize,result.length());
        }
        bufsize = minsize*2;
    }

    public StringCompositor(ParsedTemplate template, Map<String,Object> fconfig) {
        this(template,fconfig,Object::toString);
    }

    @Override
    public String apply(long value) {
        StringBuilder sb = new StringBuilder(bufsize);
        String[] ary = new String[mappers.length];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = stringfunc.apply(mappers[i].apply(value));
        }
        for (int i = 0; i < LUT.length; i++) {
          sb.append(spans[i]).append(ary[LUT[i]]);
        }
        sb.append(spans[spans.length-1]);
        return sb.toString();
    }
}
