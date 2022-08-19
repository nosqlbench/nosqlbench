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

package io.nosqlbench.virtdata.userlibs.apps.summarizer;

import io.nosqlbench.api.spi.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.IntFunction;
import java.util.function.Supplier;

@Command(
    name = "summarize-values",
    description = "Summarize the range of values provided by a binding function",
    helpCommand = true,
    showDefaultValues = true
)
@Service(value = BundledApp.class, selector = "summarize-values")
public class ValueSummarizerApp implements BundledApp, Callable<Integer> {

    private final static Logger logger = LogManager.getLogger(ValueSummarizerApp.class);

    @Option(
        names = {"loglevel"},
        description = {"The level at which to log diagnostic lines."}
    )
    Level level = Level.DEBUG;

    @Option(
        names = {"cycles"},
        description = "The cycle range, in <count> or <start>..<end> format. This is a closed-open interval as in [x,y)"
    )
    String cycles = "1";

    @Option(
        names = {"binding"},
        description = "The binding recipe to test, as it would be found in a workload YAML"
    )
    String binding = "ToString()";

    @Option(
        names = {"type"},
        description = "The object type to assert on binding output. This is 'Object' by default."
    )
    Class<?> type = Object.class;

    @Option(
        names = {"threads"},
        description = "The number of threads to spread the cycles over"
    )
    int threads = 1;

    private DataMapper<Object> mapper;
    private Supplier<DataSetSummary<?>> summarySupplier;
    private final List<DataSetSummary<?>> summaries = new ArrayList<>();

    public static void main(String[] args) {
        int result = new ValueSummarizerApp().applyAsInt(args);
        System.exit(result);
    }

    @Override
    public int applyAsInt(String[] args) {
        return new CommandLine(new ValueSummarizerApp()).execute(args);
    }

    @Override
    public Integer call() throws Exception {
        this.mapper = VirtData.getOptionalMapper(binding, type, Map.of()).orElseThrow(
            () -> new RuntimeException("Unable to find a binding for '" + binding + " of type '" + type.getSimpleName() + "'")
        );
        Object value = mapper.get(1L);

        if (value instanceof Character) {
            summarySupplier = () -> new DataSetSummary<Character>(c -> c);
        } else if (value instanceof Integer) {
            summarySupplier = () -> new DataSetSummary<Integer>(i -> i);
        } else if (value instanceof Short) {
            summarySupplier = () -> new DataSetSummary<Short>(s -> s);
        } else if (value instanceof Float) {
            summarySupplier = () -> new DataSetSummary<Float>(f -> f);
        } else if (value instanceof Long) {
            summarySupplier = () -> new DataSetSummary<Long>(l -> l);
        } else if (value instanceof CharBuffer) {
            summarySupplier = () -> new DataSetSummary<>(CharBuffer::remaining);
        } else if (value instanceof CharSequence) {
            summarySupplier = () -> new DataSetSummary<>(CharSequence::length);
        } else if (value instanceof ByteBuffer) {
            summarySupplier = () -> new DataSetSummary<>(ByteBuffer::remaining);
        } else if (value instanceof Number) {
            summarySupplier = () -> new DataSetSummary<>(Number::doubleValue);
        } else {
            logger.warn("Using default 'toString().length()' summarizer for type " + type.getSimpleName());
            summarySupplier = () -> new DataSetSummary<>(o -> (long) o.toString().length());
        }

        IntFunction<Runnable> tasks = this::taskForThreadIdx;
        StageManager stage = new StageManager(threads, tasks);
        stage.run();
        for (DataSetSummary<?> summary : summaries) {
            DoubleSummaryStatistics stats = summary.getSummaryStats();
            System.out.println(summary);
            logger.log(level, summary);
        }
        DoubleSummaryStatistics summary = summaries.stream().map(DataSetSummary::getSummaryStats).reduce((l, r) -> {
            l.combine(r);
            return l;
        }).get();
        System.out.println("combined:" + summary);

        return 0;
    }

    private Runnable taskForThreadIdx(int idx) {
        DataSetSummary<?> summary = summarySupplier.get();
        summaries.add(summary);
        long start=computeOffset(startCycle(),endCycle(),threads,idx);
        long end=computeOffset(startCycle(),endCycle(),threads,idx+1);

        return new ValuesTask(start, end, mapper, summary);
    }

    private long computeOffset(long startIncl, long endExcl, int participants, int slot) {
        long total = endExcl - startIncl;
        long div = total / participants;
        long mod = total % participants;
        long offset = div * slot + ((slot <= mod) ? slot : mod);
        return offset;
    }

    private long endCycle() {
        return Long.parseLong(cycles.contains("..") ? cycles.substring(cycles.indexOf("..") + 2) : cycles);
    }

    private long startCycle() {
        return Long.parseLong(cycles.contains("..") ? cycles.substring(0, cycles.indexOf("..")) : "0");
    }

}
