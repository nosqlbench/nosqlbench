package io.nosqlbench.virtdata.library.basics.shared.from_double.to_long;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleLong;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbLongDouble;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.EmpiricalDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleToLongFunction;

/// Empirical Histribution is a portmanteau name to capture the
/// concept of an empirical distribution based on a discrete histogram.
/// This is in contrast to the other similar method [EmpiricalDistribution],
/// which uses a continuous density estimation. Both excel in specific ways.
///
/// Use this distribution when you have a set of label frequencies which you
/// want to represent accurately.
@ThreadSafeMapper
@Categories(Category.distributions)
public class UnitHistribution extends AliasSamplerDoubleLong implements DoubleToLongFunction {

    @Example({"UnitHistribution('50 25 13 12')", "implied frequencies of 0:50 1:25 2:13 3:12"})
    @Example({
        "UnitHistribution('234:50 33:25 17:13 3:12')",
        "labeled frequencies; 234,33,17,3 are labels, and 50,25,13,12 are weights"
    })
    public UnitHistribution(String freqs) {
        List<EvProbLongDouble> events = new ArrayList<>();
        boolean labeled = (freqs.contains(":"));

        String[] elems = freqs.split("[,; ]");
        for (int i = 0; i < elems.length; i++) {
            String[] parts = elems[i].split(":", 2);
            if ((parts.length == 1 && labeled) || (parts.length == 2 && !labeled)) {
                throw new RuntimeException(
                    "If any elements are labeled, all elements must be:" + freqs);
            }
            long id = labeled ? Long.parseLong(parts[0]) : i;
            long weight = Long.parseLong(parts[labeled ? 1 : 0]);
            events.add(new EvProbLongDouble(id, weight));
        }
        super(events);
    }

    //    public UnitHistribution(long... freqs) {
    //        super(genEvents(freqs));
    //    }
    //
    //    private static List<EvProbLongDouble> genEvents(long[] freqs) {
    //        ArrayList<EvProbLongDouble> events = new ArrayList<>();
    //        for (int i = 0; i < freqs.length; i++) {
    //            events.add(new EvProbLongDouble(i, freqs[i]));
    //        }
    //        return events;
    //    }
}
