package io.nosqlbench.virtdata.library.basics.shared.distributions;

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


import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleInt;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbD;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashInterval;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.ScaledDouble;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ThreadSafeMapper
@Categories(Category.distributions)
public class WeightedInts implements LongToIntFunction {
    private final AliasSamplerDoubleInt sampler;
    private final LongToIntFunction function;

    public WeightedInts(String spec, String... modifiers) {
        sampler = new AliasSamplerDoubleInt(parseWeights(spec));
        this.function = applyModifiers(sampler, modifiers);
    }

    private LongToIntFunction applyModifiers(AliasSamplerDoubleInt aliasSampler, String[] modifiers) {

        String mode = "hash";

        for (String modifier : modifiers) {
            switch (modifier) {
                case "map":
                    mode = "map";
                    break;
                default:
                    throw new RuntimeException("Unrecognized modifier: " + modifier);
            }
        }

        if (mode.equals("hash")) {
            HashInterval f2 = new HashInterval(0.0d, 1.0d);
            return (long l) -> aliasSampler.applyAsInt(f2.applyAsDouble(l));
        } else if (mode.equals("map")) {
            ScaledDouble f1 = new ScaledDouble();
            return (long l) -> aliasSampler.applyAsInt(f1.applyAsDouble(l));
        } else {
            throw new BasicError("Unable to determine mapping mode for weighted ints function");
        }
    }

    private final static Pattern weight = Pattern.compile(
        "(?<value>\\d+)(:(?<weight>[0-9.]+))?([; ,]+)?"
    );

    private List<EvProbD> parseWeights(String spec) {
        List<EvProbD> events = new ArrayList<>();
        Matcher matcher = weight.matcher(spec);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group("value"));
            String weightSpec = matcher.group("weight");
            double weight = (weightSpec != null) ? Double.parseDouble(weightSpec) : 1.0d;
            events.add(new EvProbD(value, weight));
        }
        return events;
    }

    @Override
    public int applyAsInt(long value) {
        return function.applyAsInt(value);
    }
}
