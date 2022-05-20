package io.nosqlbench.engine.extensions.optimizers;

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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MVParams implements Iterable<MVParams.MVParam> {
    private final List<MVParam> paramList = new ArrayList<>();

    public MVParams addParam(String name, double min, double max) {
        paramList.add(new MVParam(name,min,max));
        return this;
    }

    public int size() {
        return paramList.size();
    }

    @Override
    public Iterator<MVParam> iterator() {
        return paramList.iterator();
    }

    public MVParam get(int index) {
        return paramList.get(index);
    }

    public static class MVParam {
        public final String name;
        public final double min;
        public final double max;

        public MVParam(String name, double min, double max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return
                min + "<=" + name + "<="+ max;
        }
    }

    @Override
    public String toString() {
        return paramList.stream().map(MVParam::toString).collect(Collectors.joining(" "));
    }
}
