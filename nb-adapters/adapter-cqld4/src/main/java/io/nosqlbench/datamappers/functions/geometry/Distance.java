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

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Create a Distance generator which produces
 * com.datastax.driver.dse.geometry.Distance objects.
 */
@ThreadSafeMapper
@Categories({Category.objects})
public class Distance implements LongFunction<com.datastax.dse.driver.internal.core.data.geometry.Distance> {

    private final Point pointfunc;
    private final LongToDoubleFunction rfunc;

    public Distance(LongToDoubleFunction xfunc, LongToDoubleFunction yfunc, LongToDoubleFunction rfunc) {
        pointfunc = new Point(xfunc,yfunc);
        this.rfunc = rfunc;
    }

    public Distance(double x, LongToDoubleFunction yfunc, LongToDoubleFunction rfunc) {
        pointfunc = new Point((u)->x,yfunc);
        this.rfunc = rfunc;
    }

    public Distance(LongToDoubleFunction xfunc, double y, LongToDoubleFunction rfunc) {
        pointfunc = new Point(xfunc,(v)->y);
        this.rfunc = rfunc;
    }

    public Distance(double x, double y, LongToDoubleFunction rfunc) {
        pointfunc = new Point((u)->x,(v)->y);
        this.rfunc = rfunc;
    }

    public Distance(LongToDoubleFunction xfunc, LongToDoubleFunction yfunc, double r) {
        pointfunc = new Point(xfunc,yfunc);
        this.rfunc = (w) -> r;
    }

    public Distance(double x, LongToDoubleFunction yfunc, double r) {
        pointfunc = new Point((u)->x,yfunc);
        this.rfunc = (w) -> r;
    }

    public Distance(LongToDoubleFunction xfunc, double y, double r) {
        pointfunc = new Point(xfunc,(v)->y);
        this.rfunc = (w) -> r;
    }

    public Distance(double x, double y, double r) {
        pointfunc = new Point((u) -> x, (v) -> y);
        this.rfunc = (w) -> r;
    }


    @Override
    public com.datastax.dse.driver.internal.core.data.geometry.Distance apply(long value) {
        com.datastax.dse.driver.api.core.data.geometry.Point apoint = pointfunc.apply(value);
        double aradius = rfunc.applyAsDouble(value);
        return new com.datastax.dse.driver.internal.core.data.geometry.Distance(apoint,aradius);
    }
}
