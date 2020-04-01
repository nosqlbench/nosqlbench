package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.Binder;
import io.nosqlbench.virtdata.core.bindings.Bindings;

public class CSVBindings implements Binder<String> {

    private Bindings bindings;
    private int bufferlen=0;

    public CSVBindings(Bindings bindings) {
        this.bindings = bindings;
    }

    @Override
    public String bind(long value) {
        Object[] all = bindings.getAll(value);
        StringBuilder sb = new StringBuilder();
        for (Object o : all) {

            sb.append(o.toString());
            sb.append(",");
        }
        sb.setLength(sb.length()-1);
        if (sb.length()>bufferlen) {
            bufferlen=sb.length()+5;
        }

        return sb.toString();
    }
}
