package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

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


import com.datastax.driver.dse.geometry.Point;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a com.datastax.driver.dse.geometry.Polygon
 */
@SuppressWarnings("ALL")
@ThreadSafeMapper
@Categories({Category.objects})
public class Polygon implements LongFunction<com.datastax.driver.dse.geometry.Polygon> {

    private final LongFunction<Point> pointfunc;
    private final LongToIntFunction lenfunc;

    public Polygon(LongToIntFunction lenfunc, LongFunction<Point> pointfunc) {
        this.pointfunc = pointfunc;
        this.lenfunc = lenfunc;
    }

    public Polygon(LongToIntFunction lenfunc, LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.lenfunc = lenfunc;
        this.pointfunc=new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,yfunc);
    }

    public Polygon(int len, LongFunction<Point> pointfunc) {
        this.lenfunc = (i) -> len;
        this.pointfunc = pointfunc;
    }

    @Override
    public com.datastax.driver.dse.geometry.Polygon apply(long value) {
        int linelen = Math.max(lenfunc.applyAsInt(value),3);
        Point p0 = pointfunc.apply(value);
        Point p1 = pointfunc.apply(value+1);
        Point p2 = pointfunc.apply(value+2);
        Point[] points = new Point[linelen-3];

        for (int i = 3; i < linelen; i++) {
            points[i-3]=pointfunc.apply(value+i);
        }
        return new com.datastax.driver.dse.geometry.Polygon(p0,p1,p2,points);
    }
}
