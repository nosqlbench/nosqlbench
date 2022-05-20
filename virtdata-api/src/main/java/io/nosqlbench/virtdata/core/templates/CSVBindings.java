package io.nosqlbench.virtdata.core.templates;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
