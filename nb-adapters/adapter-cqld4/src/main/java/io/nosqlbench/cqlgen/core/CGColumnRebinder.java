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

package io.nosqlbench.cqlgen.core;

import io.nosqlbench.cqlgen.binders.Binding;
import io.nosqlbench.cqlgen.binders.BindingsAccumulator;
import io.nosqlbench.cqlgen.model.CqlTableColumn;
import io.nosqlbench.cqlgen.model.ColumnPosition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CGColumnRebinder {

    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/rebinder");
    private final BindingsAccumulator accumulator;
    private final double partitionMultiplier;
    private final int quantizerDigits;

    CGColumnRebinder(BindingsAccumulator accumulator, double partitionMultipler, int quantizerDigits) {
        this.accumulator = accumulator;
        this.partitionMultiplier = partitionMultipler;
        this.quantizerDigits = quantizerDigits;
    }

    public Binding forColumn(CqlTableColumn cdef) {
        if (cdef.getPosition()== ColumnPosition.Partitioning) {
            return dividedBinding(cdef);
        } else {
            return accumulator.forColumn(cdef);
        }
    }

    private Binding dividedBinding(CqlTableColumn column) {
        CGTableStats stats = column.getTable().getTableAttributes();
        if (stats == null) {
            return accumulator.forColumn(column);
        }
        String partitionsSpec = stats.getAttribute("Number of partitions (estimate)");
        if (partitionsSpec == null) {
        }
        double estimatedPartitions = Double.parseDouble(partitionsSpec);
        long modulo = (long) (estimatedPartitions *= partitionMultiplier);
        if (modulo == 0) {
            return accumulator.forColumn(column);
        }
        modulo = quantizeModuloByMagnitude(modulo, 1);
        logger.debug("Set partition modulo for " + column.getFullName() + " to " + modulo);
        Binding binding = accumulator.forColumn(column, "Mod(" + modulo + "L); ");
        return binding;
    }

    public static long quantizeModuloByMagnitude(long modulo, int significand) {
        double initial = modulo;
        double log10 = Math.log10(initial);
        int zeroes = (int) log10;
        zeroes = Math.max(1, zeroes - (significand - 1));
        long fractional = (long) Math.pow(10, zeroes);
        long partial = ((long) initial / fractional) * fractional;
        long nextPartial = partial + fractional;
        if (Math.abs(initial - partial) <= Math.abs(initial - nextPartial)) {
            return partial;
        } else {
            return nextPartial;
        }
    }


}
