/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.datamappers.functions.geometry;

//import com.datastax.driver.dse.geometry.Point;

import com.datastax.dse.driver.internal.core.data.geometry.DefaultLineString;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a com.datastax.driver.dse.geometry.LineString
 */
@SuppressWarnings("Duplicates")
@ThreadSafeMapper
@Categories({Category.objects})
public class LineString implements LongFunction<com.datastax.dse.driver.api.core.data.geometry.LineString > {

    private final LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point > pointfunc;
    private final LongToIntFunction lenfunc;

    public LineString(LongToIntFunction lenfunc, LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point > pointfunc) {
        this.pointfunc = pointfunc;
        this.lenfunc = lenfunc;
    }

    public LineString(LongToIntFunction lenfunc, LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.lenfunc = lenfunc;
        this.pointfunc=new Point(xfunc,yfunc);
    }

    public LineString(int len, LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point > pointfunc) {
        this.lenfunc = (i) -> len;
        this.pointfunc = pointfunc;
    }

    @Override
    public com.datastax.dse.driver.api.core.data.geometry.LineString apply(long value) {
        int linelen = Math.max(lenfunc.applyAsInt(value),2);
        com.datastax.dse.driver.api.core.data.geometry.Point  p0 = pointfunc.apply(value);
        com.datastax.dse.driver.api.core.data.geometry.Point  p1 = pointfunc.apply(value+1);

        com.datastax.dse.driver.api.core.data.geometry.Point[] points =
            new com.datastax.dse.driver.api.core.data.geometry.Point [linelen-2];

        for (int i = 2; i < linelen; i++) {
            points[i-2]=pointfunc.apply(value+i);
        }
        return new DefaultLineString(p0,p1,points);
    }
}
