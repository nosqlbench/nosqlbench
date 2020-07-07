/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.nb.api.testutils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Perf is a testing utility class that collects and analyzes
 * performance data from individual test runs.
 */
public class Perf implements Iterable<Result> {
    private final String description;
    private List<Result> results = new ArrayList<>();

    public Perf(String description) {
        this.description = description;
    }


    /**
     * Get the differences between successive test runs for a given
     * property. The values provided have the same size as the results,
     * but the first result will always be Double.NaN. This makes it
     * easy to takeUpTo the results in tabular form and display them
     * "as of" a given result index.
     *
     * @param resultProperty A function that yields a double from a Result
     * @return an array of deltas of that property
     */
    public double[] getDeltas(Function<Result, Double> resultProperty) {

        double[] values = new double[results.size()];
        for (int i = 0; i < results.size(); i++) {
            values[i] = (i == 0) ? Double.NaN : resultProperty.apply(results.get(i)) - resultProperty.apply(results.get(i - 1));
        }
        return values;
    }

    /**
     * Add a test result to this performance collector.
     * @param result a {@link Result} object
     * @return this Perf, for method chaining
     */
    public Perf add(Result result) {
        this.results.add(result);
        return this;
    }

    /**
     * Add a test result to this performance collector.
     * @param description A description of the result
     * @param start The start time of the test run
     * @param end The end time of the test run
     * @param ops The total number of iterations of the test run
     * @return this Perf, for method chaining
     */
    public Perf add(String description, long start, long end, long ops) {
        return this.add(new Result(description, start, end, ops));
    }

    /**
     * Extract the double field value from the last results and return whether or not
     * they are within some fractional margin between the minimum and maximum seen value.
     * @param resultProperty A function to extract the double field value
     * @param withinMargin A value like 0.01f to represent "10 percent"
     * @param count The number of recent results that must be present
     * @return true if there are at least count results, and the min and max values are within that margin
     */
    public boolean isConverged(Function<Result, Double> resultProperty, double withinMargin, int count) {
        if (results.size() < (count + 1)) {
            return false;
        }
        double actualMargin = getMaximumDelta(resultProperty, count);
        return (actualMargin < withinMargin);
    }

    /**
     * For the most recent test runs, measure the maximum difference in
     * a given property.
     * @param resultProperty A function that produces a property from a {@link Result}
     * @param count The number of recent test runs to consider
     * @return The difference between the min and max values of the property
     */
    public double getMaximumDelta(Function<Result, Double> resultProperty, int count) {
        if (results.size() < (count + 1)) {
            return Double.NaN;
        }
        double[] values = results.stream().skip(results.size()-count).map(resultProperty).mapToDouble(Double::doubleValue).toArray();
        double min = DoubleStream.of(values).min().orElse(Double.MAX_VALUE);
        double max = DoubleStream.of(values).max().orElse(Double.MIN_VALUE);
        return (max-min) / max;
    }

    /**
     * Sort the internal results according to some property
     * @param resultProperty A function that produces a property from a {@link Result}
     * @return this Perf, for method chaining
     */
    public Perf sort(Function<Result, Double> resultProperty) {
        results = results.stream().sorted(Comparator.comparing(resultProperty)).collect(Collectors.toList());
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.description + "\n");
        results.forEach(r -> {
            sb.append(r);
            sb.append("\n");
        });
        return sb.toString();
    }

    /**
     * Summarize the last results in a tabular format, with row-by-row delta included
     * for a given property.
     * @param resultProperty A function that extracts a property from a {@link Result}
     * @param deltaDescription The description of the delta column
     * @param lastN The number of recent test runs to include
     * @return A tabular representation of the test runs and the deltas for the property
     */
    public String toStringDelta(Function<Result, Double> resultProperty, String deltaDescription, int... lastN) {
        int count = (lastN.length==1 ? lastN[0] : results.size());

        double[] deltas = getDeltas(resultProperty);
        List<String> pvalues = DoubleStream.of(deltas).mapToObj(v -> String.format("%-10.3f", v)).collect(Collectors.toList());
        List<String> rvalues = Result.toString(results);
        int maxlen = pvalues.stream().mapToInt(String::length).max().orElse(0);
        maxlen = Math.max(maxlen,deltaDescription.length());

        StringBuilder sb = new StringBuilder(String.format("iter %-" + maxlen + "s  %s\n", deltaDescription, this.description));
        String rowfmt = "%03d: %-" + maxlen + "s  %s\n";
        for (int i = 0; i < results.size(); i++) {
            sb.append(String.format(rowfmt, i, pvalues.get(i), rvalues.get(i)));
        }
        return sb.toString();
    }

    /**
     * @return Returns the last result
     */
    public Result getLastResult() {
        return results.get(results.size() - 1);
    }

    /**
     * Reduce a number of independent and concurrent runs into a single summary.
     * @return A Perf with a single result
     */
    public Perf reduceConcurrent() {
        long totalOps = results.stream().mapToLong(Result::getTotalOps).sum();
        double avgStart = results.stream().mapToLong(Result::getStartNanos).average().orElse(Double.NaN);
        double avgEnd = results.stream().mapToLong(Result::getEndNanos).average().orElse(Double.NaN);
        return new Perf("summary of \" + results.size() + \" concurrent results\"")
                .add("summary of " + results.size() + " concurrent results", (long)avgStart, (long)avgEnd, totalOps);
    }

    @Override
    public Iterator<Result> iterator() {
        return results.iterator();
    }

    public Time start(String name, long ops) {
        return new Time(this, name, ops);
    }

    public static class Time implements AutoCloseable {
        private final Perf perf;
        private final long start;
        private String name;
        private long ops;

        public Time(Perf perf, String name, long ops) {
            this.name = name;
            this.ops = ops;
            this.start = System.nanoTime();
            this.perf = perf;
        }

        @Override
        public void close() {
            long end = System.nanoTime();
            perf.add(name,start,end,ops);
        }
    }
}
