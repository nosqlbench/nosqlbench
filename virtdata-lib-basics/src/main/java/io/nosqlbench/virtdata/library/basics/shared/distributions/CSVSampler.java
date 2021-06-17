package io.nosqlbench.virtdata.library.basics.shared.distributions;

import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasElementSampler;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.ElemProbD;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;

/**
 *
 * This function is a toolkit version of the {@link WeightedStringsFromCSV} function.
 * It is more capable and should be the preferred function for alias sampling over any CSV data.
 * This sampler uses a named column in the CSV data as the value. This is also referred to as the
 * <em>labelColumn</em>. The frequency of this label depends on the weight assigned to it in another named
 * CSV column, known as the <em>weightColumn</em>.
 *
 * <H3>Combining duplicate labels</H3>
 * When you have CSV data which is not organized around the specific identifier that you want to sample by,
 * you can use some combining functions to tabulate these prior to sampling. In that case, you can use
 * any of "sum", "avg", "count", "min", or "max" as the reducing function on the value in the weight column.
 * If none are specified, then "sum" is used by default. All modes except "count" and "name" require a valid weight
 * column to be specified.
 *
 * <UL>
 *     <LI>sum, avg, min, max - takes the given stat for the weight of each distinct label</LI>
 *     <LI>count - takes the number of occurrences of a given label as the weight</LI>
 *     <LI>name - sets the weight of all distinct labels to 1.0d</LI>
 * </UL>
 *
 * <H3>Map vs Hash mode</H3>
 * As with some of the other statistical functions, you can use this one to pick through the sample values
 * by using the <em>map</em> mode. This is distinct from the default <em>hash</em> mode. When map mode is used,
 * the values will appear monotonically as you scan through the unit interval of all long values.
 * Specifically, 0L represents 0.0d in the unit interval on input, and Long.MAX_VALUE represents
 * 1.0 on the unit interval.) This mode is only recommended for advanced scenarios and should otherwise be
 * avoided. You will know if you need this mode.
 *
 */
@Categories(Category.general)
@ThreadSafeMapper
public class CSVSampler implements LongFunction<String> {

    private final AliasElementSampler<String> sampler;
    private final LongUnaryOperator prefunc;
    private final static Set MODES = Set.of("map", "hash", "sum", "avg", "count", "min", "name", "max");

    /**
     * Build an efficient O(1) sampler for the given column values with respect to the weights,
     * combining equal values by summing the weights.
     *
     * @param labelColumn   The CSV column name containing the value
     * @param weightColumn  The CSV column name containing a double weight
     * @param data Sampling modes or file names. Any of map, hash, sum, avg, count are taken
     *             as configuration modes, and all others are taken as CSV filenames.
     */
    @Example({"CSVSampler('USPS','n/a','name','census_state_abbrev')",""})
    public CSVSampler(String labelColumn, String weightColumn, String... data) {

        Function<LabeledStatistic, Double> weightFunc = LabeledStatistic::sum;
        LongUnaryOperator prefunc = new Hash();
        boolean weightRequired = false;

        while (data.length > 0 && MODES.contains(data[0])) {
            String cfg = data[0];
            data = Arrays.copyOfRange(data, 1, data.length);
            switch (cfg) {
                case "map":
                    prefunc = i -> i;
                    break;
                case "hash":
                    prefunc = new Hash();
                    break;
                case "sum":
                    weightFunc = LabeledStatistic::sum;
                    weightRequired = true;
                    break;
                case "min":
                    weightFunc = LabeledStatistic::min;
                    weightRequired = true;
                    break;
                case "max":
                    weightFunc = LabeledStatistic::max;
                    weightRequired = true;
                    break;
                case "avg":
                    weightFunc = LabeledStatistic::avg;
                    weightRequired = true;
                    break;
                case "count":
                    weightFunc = LabeledStatistic::count;
                    weightRequired = false;
                    break;
                case "name":
                    weightFunc = (v) -> 1.0d;
                    weightRequired = false;
                    break;
                default:
                    throw new BasicError("Unknown cfg verb '" + cfg + "'");

            }
        }
        this.prefunc = prefunc;

        final Function<LabeledStatistic, Double> valFunc = weightFunc;

        Map<String, LabeledStatistic> entries = new HashMap<>();

        for (String filename : data) {
            if (!filename.endsWith(".csv")) {
                filename = filename + ".csv";
            }
            CSVParser csvdata = NBIO.readFileCSV(filename);

            String labelName = csvdata.getHeaderNames().stream()
                .filter(labelColumn::equalsIgnoreCase)
                .findAny().orElseThrow();

            String weightName = "none";
            if (weightRequired) {
                weightName = csvdata.getHeaderNames().stream()
                    .filter(weightColumn::equalsIgnoreCase)
                    .findAny().orElseThrow();
            }

            double weight = 1.0d;
            for (CSVRecord csvdatum : csvdata) {
                if (csvdatum.get(labelName) != null) {
                    String label = csvdatum.get(labelName);
                    if (weightRequired) {
                        String weightString = csvdatum.get(weightName);
                        weight = weightString.isEmpty() ? 1.0d : Double.parseDouble(weightString);
                    }
                    LabeledStatistic entry = new LabeledStatistic(label, weight);
                    entries.merge(label, entry, LabeledStatistic::merge);
                }
            }
        }

        List<ElemProbD<String>> elemList = entries.values()
            .stream()
            .map(t -> new ElemProbD<>(t.label, valFunc.apply(t)))
            .collect(Collectors.toList());

        this.sampler = new AliasElementSampler<String>(elemList);
    }

    @Override
    public String apply(long value) {
        value = prefunc.applyAsLong(value);
        double unitValue = (double) value / (double) Long.MAX_VALUE;
        String val = sampler.apply(unitValue);
        return val;
    }
}
