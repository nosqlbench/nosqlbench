/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.diag.optasks;

import com.codahale.metrics.Gauge;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.function.LongToDoubleFunction;

/**
 * <p>A diag gauge task allows you to create a source of metrics data for testing or demonstration.
 * You can customize the function used to produce the raw values, the number of buckets to use for
 * leavening the values over time, and the basic stat function used to summarize the buckets into
 * an aggregate double value.</p>
 *
 * <H2>Usage Notes</H2>
 * The data image for the gauge summary is updated consistently with respect to monotonic (whole step) cycle values.
 * There are a few parameters which can be adjusted in order to make the gauge data appear more realistic.
 * <UL>
 *     <LI>label - This determines the metric label, normally used as the metric family name. Default is the task name.</LI>
 *     <LI>buckets - The number of values to seed incrementally to produce a data image</LI>
 *     <LI>binding - The binding recipe to use to create the value stored in a bin for a given cycle</LI>
 *     <LI>modulo - The interval of cycle values at which a new bin value is computed and stored in a bin</LI>
 *     <LI>stat - The aggregate statistic to use when computing the gauge value: min, avg, or max</LI>
 * </UL>
 *
 * <p>The buckets are updated incrementally and consistently based on the cycle value, modulated by the modulo value.
 * When the gauge is observed, the present value of the buckets is converted to a values image and the result is
 * summarized according to the selected stat.</p>
 *
 * <p>Practical values should be selected with awareness of the op rate and the rate of change desired in
 * the metrics over time. The buckets allow for the effective rate of change over cycles to be slowed, but it
 * is recommended to keep bin counts relative low by increasing modulo instead.</p>
 *
 * <H2>Examples</H2>
 * <p>Suppose you wanted to see a moving average, where a new value is presented every second.
 * A new value every second is obviously not needed in practical scenarios, but it makes a useful basis
 * for thinking about relative rates, since the rate limiters are specified in ops/s.
 * <UL>
 *     <LI>activity rate=10 modulo=10 - a new update will be visible every second.</LI>
 *     <LI>activity rate=1000 modulo=1000 - a new gauge value will be visible every second.</LI>
 *     <LI>activity rate=1000 modulo=60000 - a new gauge value will be visible every minute.</LI>
 *     <LI>activity rate=100 modulo=100 buckets=50 stat=avg - a new value will be visible every second,
 *     however the rate of change will be reduced due to the large sample size.</LI>
 * </UL>
 *
 * <H2>Usage Notes</H2>
 * Changing the number of buckets has a different effect based on the stat. For avg, the higher the number of buckets,
 * the smaller the standard deviation of the results. For min and max, the higher the number of buckets, the more
 * extreme the value will become. This is true for uniform bindings and non-uniform binding functions as well,
 * although you can tailor the shape of the sample data as you like.
 *
 */
@Service(value= DiagTask.class,selector="gauge")
public class DiagTask_gauge extends BaseDiagTask implements Gauge<Double> {
    private final static Logger logger = LogManager.getLogger("DIAG");

    // TODO: allow for temporal filtering
    // TODO: allow for temporal cycles
    private String name;

    private Gauge<Double> gauge;
    private LongToDoubleFunction function;
    private Double sampleValue;
    private long[] cycleMixer;
    private double[] valueMixer;
    private long modulo;
    private int buckets;
    private String label;

    private enum Stats {
        min,
        avg,
        max
    }

    private Stats stat;

    @Override
    public Map<String, Object> apply(Long cycleValue, Map<String, Object> stringObjectMap) {
        long cycle = cycleValue.longValue();
        if ((cycle%modulo)==0) {
            int bin=(int)(cycle/modulo)%cycleMixer.length;
            cycleMixer[bin]=cycleValue;
            logger.debug(() -> "updating bin " + bin + " with value " + cycle + ", now:" + Arrays.toString(cycleMixer));
        }
        return stringObjectMap;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        String binding = cfg.get("binding",String.class);
        this.buckets = cfg.get("buckets",Integer.class);
        this.modulo = cfg.get("modulo",Long.class);
        this.label = cfg.getOptional("label").orElse(super.getName());
        String stat = cfg.get("stat");

        this.cycleMixer=new long[buckets];
        this.valueMixer=new double[buckets];

        this.stat=Stats.valueOf(stat);

        DataMapper<Object> mapper = VirtData.getMapper(binding, Map.of());
        Object example = mapper.get(0L);
        if (example instanceof Double) {
            this.function=l -> (double) mapper.get(l);
        } else {
            this.function= VirtDataConversions.adaptFunction(mapper,LongToDoubleFunction.class);
        }

        logger.info("Registering gauge for diag task with labels:" + getParentLabels().getLabels() + " label:" + label);
        this.gauge=parent.create().gauge(label,() -> this.sampleValue);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_gauge.class)
            .add(Param.required("name",String.class))
            .add(Param.optional("label",String.class)
                .setDescription("A metric family name override. Defaults to the op name."))
            .add(Param.defaultTo("binding","HashRange(0L,1000000L)")
                .setDescription("A binding function to derive values from"))
            .add(Param.defaultTo("buckets", "3")
                .setDescription("how many slots to maintain in the mixer to aggregate over"))
            .add(Param.defaultTo("stat","avg")
                .setRegex("min|avg|max")
                .setDescription("min, avg, or max"))
            .add(Param.defaultTo("modulo",1L)
                .setDescription("A value used to divide down the relative rate of bin updates. 100 means 100x fewer updates"))
            .asReadOnly();
    }

    @Override
    public Double getValue() {
        for (int idx = 0; idx < valueMixer.length; idx++) {
            valueMixer[idx]=function.applyAsDouble(this.cycleMixer[idx]);
        }

        double sample= switch (this.stat) {
            case min -> Arrays.stream(this.valueMixer).reduce(Math::min).getAsDouble();
            case avg -> Arrays.stream(this.valueMixer).sum()/(double)this.valueMixer.length;
            case max -> Arrays.stream(this.valueMixer).reduce(Math::max).getAsDouble();
        };
        logger.debug(() -> "sample value for " + getParentLabels().getLabels() + ": " + sample);
        return sample;
    }

    @Override
    public NBLabels getLabels() {
        return super.getLabels().and("stat",this.stat.toString());
    }
}
